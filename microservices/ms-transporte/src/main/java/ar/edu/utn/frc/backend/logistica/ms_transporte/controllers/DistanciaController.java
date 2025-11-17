package ar.edu.utn.frc.backend.logistica.ms_transporte.controllers;

import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.distancia.DistanciaConTramosResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.distancia.DistanciaResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.GoogleMapsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/v1/distancia")
@RequiredArgsConstructor
public class DistanciaController {

    private final GoogleMapsService googleMapsService;

    @GetMapping
    public ResponseEntity<DistanciaResponseDTO> calcularDistancia(
            @RequestParam Double origenLat,
            @RequestParam Double origenLng,
            @RequestParam Double destinoLat,
            @RequestParam Double destinoLng,
            @RequestParam(required = false) List<Integer> depositoIds
    ) {
        return ResponseEntity.ok(
                googleMapsService.calcularDistancia(origenLat, origenLng, destinoLat, destinoLng, depositoIds));
    }

    @GetMapping("/detallada")
    public ResponseEntity<DistanciaConTramosResponseDTO> calcularDistanciaDetallada(
            @RequestParam Double origenLat,
            @RequestParam Double origenLng,
            @RequestParam Double destinoLat,
            @RequestParam Double destinoLng,
            @RequestParam(required = false) List<Integer> depositoIds
    ) {
        return ResponseEntity.ok(
                googleMapsService.calcularDistanciaConTramos(origenLat, origenLng, destinoLat, destinoLng, depositoIds));
    }
}
