package ar.edu.utn.frc.backend.logistica.ms_transporte.dto.camion;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CamionCreateRequestDTO {
    @NotBlank(message = "El dominio es obligatorio")
    private String dominio;

    @NotNull(message = "La capacidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La capacidad debe ser mayor a 0")
    private Float capacidad;

    @NotNull(message = "El volumen es obligatorio")
    @DecimalMin(value = "0.01", message = "El volumen debe ser mayor a 0")
    private Float volumen;

    @NotNull(message = "El costo es obligatorio")
    @DecimalMin(value = "0.01", message = "El costo debe ser mayor a 0")
    private BigDecimal costoBase;

    @NotNull(message = "El consumo es obligatorio")
    @DecimalMin(value = "0.01", message = "El consumo debe ser mayor a 0")
    private Float consumoCombustible;
}