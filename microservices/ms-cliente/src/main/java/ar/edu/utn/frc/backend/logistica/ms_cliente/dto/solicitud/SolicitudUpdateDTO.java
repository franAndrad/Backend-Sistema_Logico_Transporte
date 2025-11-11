package ar.edu.utn.frc.backend.logistica.ms_cliente.dto.solicitud;

import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.SolicitudEstado;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudUpdateDTO {
    @NotNull(message = "El estado es obligatorio")
    private SolicitudEstado estado; // PROGRAMADA, ASIGNADA, etc.

    private Integer tarifaId; // opcional según estado
}

