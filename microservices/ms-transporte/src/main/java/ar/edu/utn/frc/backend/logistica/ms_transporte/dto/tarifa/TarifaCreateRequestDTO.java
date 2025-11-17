package ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tarifa;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarifaCreateRequestDTO {
    @NotBlank(message = "El concepto es obligatorio")
    private String concepto;

    @NotNull @DecimalMin(value = "0.01", message = "Valor base > 0")
    private Float valorBase;

    @NotNull @DecimalMin(value = "0.01", message = "Valor por km > 0")
    private Float valorPorKm;

    @NotNull @DecimalMin(value = "0.01", message = "Valor por peso > 0")
    private Float valorPorPeso;

    @NotNull @DecimalMin(value = "0.01", message = "Valor por volumen > 0")
    private Float valorPorVolumen;

    @NotNull @DecimalMin(value = "0.01", message = "Valor por tramo > 0")
    private Float valorPorTramo;

    @NotNull @DecimalMin(value = "0.01", message = "Valor litro combustible > 0")
    private Float valorLitroCombustible;

    @NotNull(message = "Fecha vigencia obligatoria")
    private LocalDate fechaVigencia;
}

