package ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tarifa;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarifaUpdateRequestDTO {
    @NotNull @DecimalMin(value = "0.01")
    private Float valorBase;
    @NotNull @DecimalMin(value = "0.01")
    private Float valorPorKm;
    @NotNull @DecimalMin(value = "0.01")
    private Float valorPorPeso;
    @NotNull @DecimalMin(value = "0.01")
    private Float valorPorVolumen;
    @NotNull @DecimalMin(value = "0.01")
    private Float valorPorTramo;
    @NotNull @DecimalMin(value = "0.01")
    private Float valorLitroCombustible;
    @NotNull
    private LocalDate fechaVigencia;
    @NotNull
    private Boolean activo;
}

