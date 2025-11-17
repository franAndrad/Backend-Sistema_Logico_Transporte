package ar.edu.utn.frc.backend.logistica.ms_cliente.client.transporte.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistanciaResponseDto {
    private Double origenLat;
    private Double origenLng;
    private Double destinoLat;
    private Double destinoLng;
    private Double distanciaKm;
    private Long duracionMinutos;
    private BigDecimal costoEstimadoTotal;
}

