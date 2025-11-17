package ar.edu.utn.frc.backend.logistica.ms_transporte.dto.ruta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegCalculadoDTO {
    private BigDecimal distanciaKm;
    private Long duracionMin;
    private Integer depositoOrigenId;
    private Integer depositoDestinoId; 
    private String tipo;
    private BigDecimal costoEstimado;
}
