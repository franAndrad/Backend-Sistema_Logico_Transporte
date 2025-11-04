package ar.edu.utn.frc.backend.logistica.ms_transporte.controllers;

import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.DistanciaResponse;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.GoogleMapsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/distancia")
@RequiredArgsConstructor
public class DistanciaController {
    
    private final GoogleMapsService googleMapsService;
    
    @GetMapping
    public ResponseEntity<DistanciaResponse> calcularDistancia(
        @RequestParam Double origenLat,
        @RequestParam Double origenLng,
        @RequestParam Double destinoLat,
        @RequestParam Double destinoLng
    ) {
        return ResponseEntity.ok(
            googleMapsService.calcularDistancia(origenLat, origenLng, destinoLat, destinoLng)
        );
    }
}
