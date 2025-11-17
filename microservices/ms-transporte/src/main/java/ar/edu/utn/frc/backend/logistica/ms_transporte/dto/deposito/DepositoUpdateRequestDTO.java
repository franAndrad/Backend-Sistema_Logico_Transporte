package ar.edu.utn.frc.backend.logistica.ms_transporte.dto.deposito;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositoUpdateRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @DecimalMin(value = "0.01", message = "El costo debe ser mayor a 0")
    @NotNull(message = "El costo es obligatorio")
    private BigDecimal costoEstadiaDiario;

    @DecimalMin(value = "-90.0", message = "Latitud mínima permitida -90")
    @DecimalMax(value = "90.0", message = "Latitud máxima permitida 90")
    @NotNull(message = "La latitud es obligatoria")
    private BigDecimal latitud;

    @DecimalMin(value = "-180.0", message = "Longitud mínima permitida -180")
    @DecimalMax(value = "180.0", message = "Longitud máxima permitida 180")
    @NotNull(message = "La longitud es obligatoria")
    private BigDecimal longitud;

    private Boolean activo;
}