package ar.edu.utn.frc.backend.logistica.ms_cliente.dto.solicitud;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudResponseDTO {
    private Integer id;
    private String mensaje;
}

