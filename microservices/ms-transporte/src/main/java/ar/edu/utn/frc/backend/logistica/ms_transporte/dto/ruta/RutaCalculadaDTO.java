package ar.edu.utn.frc.backend.logistica.ms_transporte.dto.ruta;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutaCalculadaDTO {
    private BigDecimal distanciaTotalKm;
    private Long duracionTotalMin;
    private List<LegCalculadoDTO> legs;
}


