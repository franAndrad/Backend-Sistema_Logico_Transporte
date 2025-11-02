package ar.edu.utn.frc.backend.logistica.ms_transporte.controllers;

import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.DistanciaResponse;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.DistanciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/distancias")
@RequiredArgsConstructor
public class DistanciaController {
    
    private final DistanciaService distanciaService;
    
    @GetMapping("/calcular")
    public ResponseEntity<DistanciaResponse> calcularDistancia(
        @RequestParam Double origenLat,
        @RequestParam Double origenLng,
        @RequestParam Double destinoLat,
        @RequestParam Double destinoLng
    ) {
        Double distanciaKm = distanciaService.calcularDistanciaKm(
            origenLat, origenLng, destinoLat, destinoLng
        );
        
        Long duracionMinutos = distanciaService.calcularDuracionMinutos(
            origenLat, origenLng, destinoLat, destinoLng
        );
        
        DistanciaResponse response = new DistanciaResponse(
            origenLat, origenLng,
            destinoLat, destinoLng,
            distanciaKm,
            duracionMinutos
        );
        
        return ResponseEntity.ok(response);
    }
}
