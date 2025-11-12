package ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tramo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TramoCreateResponseDTO {
    private Integer idTramo;
    private String mensaje;
}
