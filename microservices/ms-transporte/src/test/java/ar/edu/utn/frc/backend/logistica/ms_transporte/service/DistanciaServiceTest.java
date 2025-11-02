package ar.edu.utn.frc.backend.logistica.ms_transporte.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.client.GoogleMapsClient;
import ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto.DirectionsResponse;
import ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto.DirectionsResponse.Distance;
import ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto.DirectionsResponse.Duration;
import ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto.DirectionsResponse.Leg;
import ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto.DirectionsResponse.Route;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DistanciaServiceTest {

    @Mock
    private GoogleMapsClient googleMapsClient;

    @InjectMocks
    private DistanciaService distanciaService;

    @Test
    void calcularDistanciaKm_DeberiaRetornarDistanciaCorrecta() {
        DirectionsResponse mockResponse = crearRespuestaMock(100000L, 3600L); // 100 km, 1 hora
        
        when(googleMapsClient.getDirections(
            anyString(), 
            anyString(), 
            anyString(),
            anyBoolean(),  
            anyString()    
        )).thenReturn(mockResponse);

        Double distancia = distanciaService.calcularDistanciaKm(
            -34.603722, -58.381592,
            -34.921230, -57.954590
        );

        assertEquals(100.0, distancia);
    }

    @Test
    void calcularDuracionMinutos_DeberiaRetornarDuracionCorrecta() {
        DirectionsResponse mockResponse = crearRespuestaMock(100000L, 3600L); // 100 km, 1 hora
        
        when(googleMapsClient.getDirections(
            anyString(), 
            anyString(), 
            anyString(),
            anyBoolean(),  
            anyString()    
        )).thenReturn(mockResponse);


        Long duracion = distanciaService.calcularDuracionMinutos(
            -34.603722, -58.381592,
            -34.921230, -57.954590
        );

        assertEquals(60L, duracion); // 3600 segundos = 60 minutos
    }

    @Test
    void calcularDistanciaKm_ConErrorDeApi_DeberiaLanzarExcepcion() {
        DirectionsResponse mockResponse = new DirectionsResponse();
        mockResponse.setStatus("NOT_FOUND");
        
        when(googleMapsClient.getDirections(
            anyString(), 
            anyString(), 
            anyString(),
            anyBoolean(),
            anyString()   
        )).thenReturn(mockResponse);

        assertThrows(RuntimeException.class, () -> {
            distanciaService.calcularDistanciaKm(
                -34.603722, -58.381592,
                -34.921230, -57.954590
            );
        });
    }

    @Test
    void calcularDistanciaKm_ConRutasVacias_DeberiaLanzarExcepcion() {
        // Arrange
        DirectionsResponse mockResponse = new DirectionsResponse();
        mockResponse.setStatus("OK");
        mockResponse.setRoutes(List.of()); // Lista vacía
        
        when(googleMapsClient.getDirections(
            anyString(), 
            anyString(), 
            anyString(),
            anyBoolean(),
            anyString()
        )).thenReturn(mockResponse);

        assertThrows(RuntimeException.class, () -> {
            distanciaService.calcularDistanciaKm(
                -34.603722, -58.381592,
                -34.921230, -57.954590
            );
        });
    }

    // Método helper para crear respuestas mock
    private DirectionsResponse crearRespuestaMock(Long distanciaMetros, Long duracionSegundos) {
        DirectionsResponse response = new DirectionsResponse();
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