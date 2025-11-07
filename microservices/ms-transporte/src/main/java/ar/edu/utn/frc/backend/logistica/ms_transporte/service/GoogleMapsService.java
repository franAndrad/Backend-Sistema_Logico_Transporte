package ar.edu.utn.frc.backend.logistica.ms_transporte.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.client.GoogleMapsClient;
import ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto.DirectionsResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.DistanciaResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleMapsService {

    private final GoogleMapsClient googleMapsClient;

    @Value("${google.maps.api.key}")
    private String apiKey;

    public DistanciaResponseDTO calcularDistancia(Double origenLat, Double origenLng, 
                                                Double destinoLat, Double destinoLng) {
        
        String origin = origenLat + "," + origenLng;
        String destination = destinoLat + "," + destinoLng;

        DirectionsResponseDTO response = googleMapsClient.getDirections(
            origin, 
            destination, 
            "driving", 
            false, 
            apiKey
        );

        if (!"OK".equals(response.getStatus())) {
            throw new RuntimeException("Error en Google Maps API: " + response.getStatus());
        }

        DirectionsResponseDTO.Leg leg = response.getRoutes().get(0).getLegs().get(0);
        
        double distanciaKm = leg.getDistance().getValue() / 1000.0;
        long duracionMinutos = leg.getDuration().getValue() / 60;

        return new DistanciaResponseDTO(
            origenLat, origenLng, 
            destinoLat, destinoLng,
            distanciaKm, duracionMinutos
        );
    }
}