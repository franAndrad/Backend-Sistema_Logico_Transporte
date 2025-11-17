package ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudEstadoUpdateRequestDTO {
    private String estado;
    private String descripcion;
}

