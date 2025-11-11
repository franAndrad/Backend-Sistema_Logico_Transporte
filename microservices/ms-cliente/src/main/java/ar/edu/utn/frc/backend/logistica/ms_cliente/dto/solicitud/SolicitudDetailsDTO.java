package ar.edu.utn.frc.backend.logistica.ms_cliente.dto.solicitud;

import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.SolicitudEstado;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudDetailsDTO {
    private Integer id;
    private String origenDireccion;
    private String destinoDireccion;
    private SolicitudEstado estado;
    private Double costoEstimado;
    private Integer tiempoEstimado;
    private String contenedorIdentificacion;
}

