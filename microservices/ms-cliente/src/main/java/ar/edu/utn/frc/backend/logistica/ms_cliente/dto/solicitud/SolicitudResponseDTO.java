package ar.edu.utn.frc.backend.logistica.ms_cliente.dto.solicitud;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudResponseDTO {
    private Integer id;
    private String mensaje;
}

