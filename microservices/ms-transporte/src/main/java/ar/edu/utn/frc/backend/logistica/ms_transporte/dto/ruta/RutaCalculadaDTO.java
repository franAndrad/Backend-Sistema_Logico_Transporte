package ar.edu.utn.frc.backend.logistica.ms_transporte.dto.ruta;

import java.math.BigDecimal;
import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutaCalculadaDTO {
    private BigDecimal distanciaTotalKm;
    private Long duracionTotalMin;
    private List<LegCalculadoDTO> legs;
}


