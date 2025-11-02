package ar.edu.utn.frc.backend.logistica.ms_transporte.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.client.GoogleMapsClient;
import ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto.DirectionsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DistanciaService {
    
    private final GoogleMapsClient googleMapsClient;
    
    @Value("${google.maps.api.key}")
    private String apiKey;
    
    public Double calcularDistanciaKm(
        Double origenLat, 
        Double origenLng,
        Double destinoLat, 
        Double destinoLng
    ) {
        
        // Formatear coordenadas en formato "lat,lng"
        String origin = String.format("%f,%f", origenLat, origenLng);
        String destination = String.format("%f,%f", destinoLat, destinoLng);
        
        // Llamar a Google Maps API (o WireMock en desarrollo)
        DirectionsResponse response = googleMapsClient.getDirections(
            origin, 
            destination, 
            apiKey
        );
        
        if (!"OK".equals(response.getStatus())) {
            throw new RuntimeException("Error al calcular distancia: " + response.getStatus());
        }
        
        if (response.getRoutes() == null || response.getRoutes().isEmpty()) {
            throw new RuntimeException("No se encontraron rutas");
        }
        
        Long distanciaMetros = response.getRoutes().get(0)
            .getLegs().get(0)
            .getDistance()
            .getValue();
        
        Double distanciaKm = distanciaMetros / 1000.0;
                
        return distanciaKm;
    }
    
    public Long calcularDuracionMinutos(
        Double origenLat, 
        Double origenLng,
        Double destinoLat, 
        Double destinoLng
    ) {
        String origin = String.format("%f,%f", origenLat, origenLng);
        String destination = String.format("%f,%f", destinoLat, destinoLng);
        
        DirectionsResponse response = googleMapsClient.getDirections(
            origin, 
            destination, 
            apiKey
        );
        
        if (!"OK".equals(response.getStatus())) {
            throw new RuntimeException("Error al calcular duración: " + response.getStatus());
        }
        
        if (response.getRoutes() == null || response.getRoutes().isEmpty()) {
            throw new RuntimeException("No se encontraron rutas");
        }
        
        Long duracionSegundos = response.getRoutes().get(0)
            .getLegs().get(0)
            .getDuration()
            .getValue();
        
        Long duracionMinutos = duracionSegundos / 60;
                
        return duracionMinutos;
    }
}
