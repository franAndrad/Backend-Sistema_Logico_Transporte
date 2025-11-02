package ar.edu.utn.frc.backend.logistica.ms_transporte.client;

import ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto.DirectionsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "google-maps-api",
    url = "${google.maps.api.url}"
)

// Hacemos una interface por feign (requerido), conveniente usarlo con api externas (parseo, url, manejo errores, etc)
public interface GoogleMapsClient {
    
    @GetMapping("/maps/api/directions/json")
    DirectionsResponse getDirections(
        @RequestParam("origin") String origin,
        @RequestParam("destination") String destination,
        @RequestParam("mode") String mode,
        @RequestParam("alternatives") boolean alternatives,
        @RequestParam("key") String key
    );
}