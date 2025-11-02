package ar.edu.utn.frc.backend.logistica.ms_transporte.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.client.GoogleMapsClient;
import ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto.DirectionsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@SpringBootTest
class DistanciaServiceTest {
    
    @Autowired
    private DistanciaService distanciaService;
    
    @MockBean
    private GoogleMapsClient googleMapsClient;

    // Test casa de Ale a casa de Fran
    @Test
    void testCalcularDistanciaCordobaCapitalRioPrimero(){
        DirectionsResponse mockResponse = crearRespuestaMock(1090000L,45060L);
        when(googleMapsClient.getDirections(anyString(), anyString(), anyString()))
            .thenReturn(mockResponse);

        Double distancia = distanciaService.calcularDistanciaKm(
            -31.32691, -63.61972,  // Casa Ale
            -24.388278, -65.127934 // Contruccion Fran  
        );

        assertEquals(1090.0, distancia, 0.001);
    }

    @Test
    void testCalcularDuracionCordobaCapitalRioPrimero() {
        DirectionsResponse mockResponse = crearRespuestaMock(1090000L, 45060L);
        when(googleMapsClient.getDirections(anyString(), anyString(), anyString()))
            .thenReturn(mockResponse);

        Long duracion = distanciaService.calcularDuracionMinutos(
            -31.32691, -63.61972,  // Casa Ale
            -24.388278, -65.127934 // Contruccion Fran
        );

        assertEquals(751, duracion);
    }
    
    @Test
    void testErrorCuandoNoHayRutas() {
        // Configurar mock con error
        DirectionsResponse errorResponse = new DirectionsResponse();
        errorResponse.setStatus("ZERO_RESULTS");
        errorResponse.setRoutes(List.of());
        
        when(googleMapsClient.getDirections(anyString(), anyString(), anyString()))
            .thenReturn(errorResponse);
        
        // Verificar que lanza excepción
        assertThrows(RuntimeException.class, () -> 
            distanciaService.calcularDistanciaKm(0.0, 0.0, 0.0, 0.0)
        );
    }
    
    private DirectionsResponse crearRespuestaMock(Long distanciaMetros, Long duracionSegundos) {
        DirectionsResponse response = new DirectionsResponse();
        response.setStatus("OK");
        
        DirectionsResponse.Route route = new DirectionsResponse.Route();
        DirectionsResponse.Leg leg = new DirectionsResponse.Leg();
        
        DirectionsResponse.Distance distance = new DirectionsResponse.Distance();
        distance.setValue(distanciaMetros);
        leg.setDistance(distance);
        
        DirectionsResponse.Duration duration = new DirectionsResponse.Duration();
        duration.setValue(duracionSegundos);
        leg.setDuration(duration);
        
        route.setLegs(List.of(leg));
        response.setRoutes(List.of(route));
        
        return response;
    }
}
