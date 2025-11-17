package ar.edu.utn.frc.backend.logistica.ms_transporte.dto.ruta;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutaCreateRequestDTO {

    @NotNull(message = "El ID de la solicitud es obligatorio")
    private Integer idSolicitud;

    @NotNull(message = "La latitud de origen es obligatoria")
    @DecimalMin(value = "-90.0", message = "Latitud mínima permitida -90")
    @DecimalMax(value = "90.0", message = "Latitud máxima permitida 90")
    private BigDecimal origenLat;

    @NotNull(message = "La longitud de origen es obligatoria")
    @DecimalMin(value = "-180.0", message = "Longitud mínima permitida -180")
    @DecimalMax(value = "180.0", message = "Longitud máxima permitida 180")
    private BigDecimal origenLon;

    @NotNull(message = "La latitud de destino es obligatoria")
    @DecimalMin(value = "-90.0", message = "Latitud mínima permitida -90")
    @DecimalMax(value = "90.0", message = "Latitud máxima permitida 90")
    private BigDecimal destinoLat;

    @NotNull(message = "La longitud de destino es obligatoria")
    @DecimalMin(value = "-180.0", message = "Longitud mínima permitida -180")
    @DecimalMax(value = "180.0", message = "Longitud máxima permitida 180")
    private BigDecimal destinoLon;

    private List<Integer> depositoIds;
}
