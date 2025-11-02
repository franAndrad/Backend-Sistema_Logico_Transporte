package ar.edu.utn.frc.backend.logistica.ms_transporte.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistanciaResponse {
    private Double origenLat;
    private Double origenLng;

    private Double destinoLat;
    private Double destinoLng;
    
    private Double distanciaKm;
    private Long duracionMinutos;
}
