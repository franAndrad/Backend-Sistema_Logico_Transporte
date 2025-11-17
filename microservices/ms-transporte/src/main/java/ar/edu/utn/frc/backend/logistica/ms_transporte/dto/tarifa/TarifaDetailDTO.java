package ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tarifa;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarifaDetailDTO {
    private Integer idTarifa;
    private String concepto;
    private Float valorBase;
    private Float valorPorKm;
    private Float valorPorPeso;
    private Float valorPorVolumen;
    private Float valorPorTramo;
    private Float valorLitroCombustible;
    private LocalDate fechaVigencia;
    private Boolean activo;
}

