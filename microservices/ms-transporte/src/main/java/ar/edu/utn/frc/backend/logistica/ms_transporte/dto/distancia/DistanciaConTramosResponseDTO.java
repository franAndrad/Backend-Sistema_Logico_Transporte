package ar.edu.utn.frc.backend.logistica.ms_transporte.dto.distancia;

import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.ruta.LegCalculadoDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistanciaConTramosResponseDTO {
    private Double origenLat;
    private Double origenLng;
    private Double destinoLat;
    private Double destinoLng;
    private BigDecimal distanciaKmTotal; 
    private Long duracionMinutosTotal; 
    private List<LegCalculadoDTO> tramos;
}
