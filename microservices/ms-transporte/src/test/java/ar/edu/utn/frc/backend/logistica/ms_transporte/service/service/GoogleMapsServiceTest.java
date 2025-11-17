package ar.edu.utn.frc.backend.logistica.ms_transporte.service.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.client.GoogleMapsClient;
import ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto.DirectionsResponseDTO;
// Importaciones corregidas para coincidir con el DTO real:
import ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto.DirectionsResponseDTO.Distance;
import ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto.DirectionsResponseDTO.Duration;
import ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto.DirectionsResponseDTO.Leg;
import ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto.DirectionsResponseDTO.Route;

import ar.edu.utn.frc.backend.logistica.ms_transporte.config.GoogleMapsProperties;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.ruta.LegCalculadoDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.ruta.RutaCalculadaDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Deposito;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.DepositoService;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.GoogleMapsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleMapsServiceTest {

    @Mock private GoogleMapsClient googleMapsClient;
    @Mock private GoogleMapsProperties googleMapsProperties;
    @Mock private DepositoService depositoService;

    @InjectMocks
    private GoogleMapsService googleMapsService;

    private final String API_KEY = "test_key";

    @BeforeEach
    void setup() {
        GoogleMapsProperties.Api mockApi = new GoogleMapsProperties.Api();


        mockApi.setKey(API_KEY);


        when(googleMapsProperties.getApi()).thenReturn(mockApi);
    }

    private Deposito setupDeposito(int id, double lat, double lng) {
        Deposito d = new Deposito();
        d.setIdDeposito(id);
        d.setLatitud(BigDecimal.valueOf(lat));
        d.setLongitud(BigDecimal.valueOf(lng));
        d.setActivo(true);
        return d;
    }



    private DirectionsResponseDTO createMockDirectionsResponse(String status, List<Leg> legs) {
        DirectionsResponseDTO response = new DirectionsResponseDTO();
        response.setStatus(status);
        if ("OK".equals(status)) {
            Route route = new Route();
            route.setLegs(legs);
            response.setRoutes(List.of(route));
        } else {
            response.setRoutes(new ArrayList<>());
        }
        return response;
    }


    private Leg createMockLeg(long meters, long seconds) {
        Distance distance = new Distance();
        distance.setValue(meters);

        Duration duration = new Duration();
        duration.setValue(seconds);

        Leg leg = new Leg();
        leg.setDistance(distance);
        leg.setDuration(duration);
        return leg;
    }



    @Test
    void calcularRutaYTramos_success_noWaypoints() {
        // Simula ruta directa (1 Leg)
        Leg leg = createMockLeg(10000L, 600L); // 10km, 10 min
        DirectionsResponseDTO mockResponse = createMockDirectionsResponse("OK", List.of(leg));

        when(googleMapsClient.getDirections(anyString(), anyString(), anyString(), anyBoolean(), eq(""), eq(API_KEY)))
                .thenReturn(mockResponse);

        RutaCalculadaDTO result = googleMapsService.calcularRutaYTramos(0.0, 0.0, 1.0, 1.0, null);

        assertEquals(1, result.getLegs().size());
        assertEquals(BigDecimal.valueOf(10.000).setScale(3), result.getDistanciaTotalKm());
        assertEquals(10L, result.getDuracionTotalMin());


        LegCalculadoDTO legResult = result.getLegs().get(0);
        assertEquals("ORIGEN_DESTINO", legResult.getTipo());
        assertNull(legResult.getDepositoOrigenId());
        assertNull(legResult.getDepositoDestinoId());
    }

    @Test
    void calcularRutaYTramos_success_oneWaypoint() {
        // Simula ruta con un depósito (2 Legs)
        Leg leg1 = createMockLeg(5000L, 300L); // Origen -> Deposito (5km, 5 min)
        Leg leg2 = createMockLeg(7500L, 450L); // Deposito -> Destino (7.5km, 7.5 min)
        DirectionsResponseDTO mockResponse = createMockDirectionsResponse("OK", List.of(leg1, leg2));

        List<Integer> depositoIds = List.of(1);
        Deposito d1 = setupDeposito(1, 0.5, 0.5);

        when(depositoService.findActivosByIdsKeepingOrder(depositoIds)).thenReturn(List.of(d1));
        when(googleMapsClient.getDirections(anyString(), anyString(), anyString(), anyBoolean(), anyString(), eq(API_KEY)))
                .thenReturn(mockResponse);

        RutaCalculadaDTO result = googleMapsService.calcularRutaYTramos(0.0, 0.0, 1.0, 1.0, depositoIds);

        assertEquals(2, result.getLegs().size());
        assertEquals(BigDecimal.valueOf(12.500).setScale(3), result.getDistanciaTotalKm());


        assertEquals("ORIGEN_DEPOSITO", result.getLegs().get(0).getTipo());
        assertEquals(1, result.getLegs().get(0).getDepositoDestinoId());


        assertEquals("DEPOSITO_DESTINO", result.getLegs().get(1).getTipo());
        assertEquals(1, result.getLegs().get(1).getDepositoOrigenId());
    }

    @Test
    void calcularRutaYTramos_success_twoWaypoints() {
        // Simula ruta con dos depósitos (3 Legs)
        Leg leg1 = createMockLeg(5000L, 300L); // O -> D1
        Leg leg2 = createMockLeg(1000L, 60L); // D1 -> D2
        Leg leg3 = createMockLeg(7000L, 420L); // D2 -> DEST
        DirectionsResponseDTO mockResponse = createMockDirectionsResponse("OK", List.of(leg1, leg2, leg3));

        List<Integer> depositoIds = List.of(1, 2);
        Deposito d1 = setupDeposito(1, 0.5, 0.5);
        Deposito d2 = setupDeposito(2, 0.8, 0.8);

        when(depositoService.findActivosByIdsKeepingOrder(depositoIds)).thenReturn(List.of(d1, d2));
        when(googleMapsClient.getDirections(anyString(), anyString(), anyString(), anyBoolean(), anyString(), eq(API_KEY)))
                .thenReturn(mockResponse);

        RutaCalculadaDTO result = googleMapsService.calcularRutaYTramos(0.0, 0.0, 1.0, 1.0, depositoIds);

        assertEquals("ORIGEN_DEPOSITO", result.getLegs().get(0).getTipo());
        assertEquals(1, result.getLegs().get(0).getDepositoDestinoId());


        assertEquals("DEPOSITO_DEPOSITO", result.getLegs().get(1).getTipo());
        assertEquals(1, result.getLegs().get(1).getDepositoOrigenId());
        assertEquals(2, result.getLegs().get(1).getDepositoDestinoId());


        assertEquals("DEPOSITO_DESTINO", result.getLegs().get(2).getTipo());
        assertEquals(2, result.getLegs().get(2).getDepositoOrigenId());
    }



    @Test
    void calcularRutaYTramos_googleMapsErrorStatus_throws() {
        DirectionsResponseDTO mockResponse = createMockDirectionsResponse("ZERO_RESULTS", List.of());

        when(googleMapsClient.getDirections(anyString(), anyString(), anyString(), anyBoolean(), anyString(), eq(API_KEY)))
                .thenReturn(mockResponse);

        assertThrows(RuntimeException.class, () -> googleMapsService.calcularRutaYTramos(0.0, 0.0, 1.0, 1.0, null));
    }
}