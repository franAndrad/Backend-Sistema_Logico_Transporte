package ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tarifa;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarifaCreateResponseDTO {
    private Integer idTarifa;
    private String mensaje;
}

