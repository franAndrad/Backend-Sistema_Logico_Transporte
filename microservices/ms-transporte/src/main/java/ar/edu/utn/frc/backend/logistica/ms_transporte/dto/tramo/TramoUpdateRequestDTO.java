package ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tramo;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TramoUpdateRequestDTO {

    private Integer depositoOrigenId;

    private Integer depositoDestinoId;

    @NotNull(message = "La distancia es obligatoria")
    @DecimalMin(value = "0.001", message = "La distancia debe ser mayor a 0")
    private BigDecimal distancia;

    @NotNull(message = "El costo aproximado es obligatorio")
    @DecimalMin(value = "0.01", message = "El costo aproximado debe ser mayor a 0")
    private BigDecimal costoAproximado;

    private String dominioCamion;

    private String keyCloakIdTransportista;

    private LocalDateTime fechaHoraInicioEstimada;

    private LocalDateTime fechaHoraFinEstimada;
}
