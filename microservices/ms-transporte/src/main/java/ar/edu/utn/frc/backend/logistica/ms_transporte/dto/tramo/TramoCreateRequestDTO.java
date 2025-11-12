package ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tramo;

import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.enums.TipoTramo;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TramoCreateRequestDTO {

    @NotNull(message = "El ID de la ruta es obligatorio")
    private Integer idRuta;

    private Integer depositoOrigenId;

    private Integer depositoDestinoId;

    @NotNull(message = "El tipo de tramo es obligatorio")
    private TipoTramo tipo;

    @NotNull(message = "La distancia es obligatoria")
    @DecimalMin(value = "0.001", message = "La distancia debe ser mayor a 0")
    private BigDecimal distancia;

    @NotNull(message = "El costo aproximado es obligatorio")
    @DecimalMin(value = "0.01", message = "El costo aproximado debe ser mayor a 0")
    private BigDecimal costoAproximado;

    private LocalDateTime fechaHoraInicioEstimada;

    private LocalDateTime fechaHoraFinEstimada;
}
