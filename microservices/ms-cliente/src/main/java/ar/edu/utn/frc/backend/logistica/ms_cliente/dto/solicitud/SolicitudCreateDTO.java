package ar.edu.utn.frc.backend.logistica.ms_cliente.dto.solicitud;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudCreateDTO {

    @NotNull(message = "El idCliente es obligatorio")
    private Integer idCliente;

    @NotNull(message = "El idContenedor es obligatorio")
    private Integer idContenedor;

    @NotBlank(message = "La dirección de origen es obligatoria")
    private String origenDireccion;

    @NotBlank(message = "La dirección de destino es obligatoria")
    private String destinoDireccion;

    // Coordenadas opcionales
    private Double origenLatitud;
    private Double origenLongitud;
    private Double destinoLatitud;
    private Double destinoLongitud;
}

