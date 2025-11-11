package ar.edu.utn.frc.backend.logistica.ms_cliente.dto.solicitud;

import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.SolicitudEstado;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudListDTO {
    private Integer id;
    private Integer clienteId;
    private SolicitudEstado estado;
    private Double costoEstimado;
    private LocalDateTime fechaCreacion;
}

