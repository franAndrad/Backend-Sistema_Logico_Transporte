package ar.edu.utn.frc.backend.logistica.ms_transporte.client;

import ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto.DirectionsResponseDTO;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "google-maps-api", url = "${google.maps.api.url}")
public interface GoogleMapsClient {
    
    @GetMapping("/maps/api/directions/json")
    DirectionsResponseDTO getDirections(
        @RequestParam("origin") String origin,
        @RequestParam("destination") String destination,
        @RequestParam("mode") String mode,
        @RequestParam("alternatives") boolean alternatives,
        @RequestParam(value = "waypoints", required = false) String waypoints,
        @RequestParam("key") String key
    );
}