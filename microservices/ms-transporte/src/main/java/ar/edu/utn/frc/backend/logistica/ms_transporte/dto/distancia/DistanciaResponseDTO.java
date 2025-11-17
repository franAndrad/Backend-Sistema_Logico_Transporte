package ar.edu.utn.frc.backend.logistica.ms_transporte.dto.distancia;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistanciaResponseDTO {
    private Double origenLat;
    private Double origenLng;
    private Double destinoLat;
    private Double destinoLng;
    private Double distanciaKm;
    private Long duracionMinutos;
    private BigDecimal costoEstimadoTotal;
}
