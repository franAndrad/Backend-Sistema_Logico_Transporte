package ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tarifa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarifaResponseDTO {
    private Integer idTarifa;
    private String mensaje;
}

