package ar.edu.utn.frc.backend.logistica.ms_transporte.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.client.GoogleMapsClient;
import ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto.DirectionsResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto.DirectionsResponseDTO.Distance;
import ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto.DirectionsResponseDTO.Duration;
import ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto.DirectionsResponseDTO.Leg;
import ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto.DirectionsResponseDTO.Route;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.DistanciaResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DistanciaServiceTest {

    @Mock
    private GoogleMapsClient googleMapsClient;

    @InjectMocks
    private GoogleMapsService googleMapsService;

    @Test
    void calcularDistancia_DeberiaRetornarDistanciaYDuracionCorrecta() {
        ReflectionTestUtils.setField(googleMapsService, "apiKey", "test-key");
        
        DirectionsResponseDTO mockResponse = crearRespuestaMock(100000L, 3600L);
        
        when(googleMapsClient.getDirections(
            anyString(), 
            anyString(), 
            anyString(),
            anyBoolean(),  
            anyString()    
        )).thenReturn(mockResponse);

        DistanciaResponseDTO response = googleMapsService.calcularDistancia(
            -34.603722, -58.381592,
            -34.921230, -57.954590
        );

        assertEquals(100.0, response.getDistanciaKm());
        assertEquals(60L, response.getDuracionMinutos());
    }

    @Test
    void calcularDistancia_ConErrorDeApi_DeberiaLanzarExcepcion() {
        ReflectionTestUtils.setField(googleMapsService, "apiKey", "test-key");
        
        DirectionsResponseDTO mockResponse = new DirectionsResponseDTO();
        mockResponse.setStatus("NOT_FOUND");
        
        when(googleMapsClient.getDirections(
            anyString(), 
            anyString(), 
            anyString(),
            anyBoolean(),
            anyString()   
        )).thenReturn(mockResponse);

        assertThrows(RuntimeException.class, () -> {
            googleMapsService.calcularDistancia(
                -34.603722, -58.381592,
                -34.921230, -57.954590
            );
        });
    }

    private DirectionsResponseDTO crearRespuestaMock(Long distanciaMetros, Long duracionSegundos) {
        DirectionsResponseDTO response = new DirectionsResponseDTO();
        response.setStatus("OK");

        Distance distance = new Distance();
        distance.setValue(distanciaMetros);

        Duration duration = new Duration();
        duration.setValue(duracionSegundos);

        Leg leg = new Leg();
        leg.setDistance(distance);
        leg.setDuration(duration);

        Route route = new Route();
        route.setLegs(List.of(leg));

        response.setRoutes(List.of(route));

        return response;
    }
}