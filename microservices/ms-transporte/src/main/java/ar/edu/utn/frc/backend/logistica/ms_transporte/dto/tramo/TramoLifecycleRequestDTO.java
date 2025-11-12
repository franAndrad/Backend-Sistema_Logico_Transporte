package ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tramo;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TramoLifecycleRequestDTO {

    // OPCIONAL: si no viene, se usa LocalDateTime.now()
    private LocalDateTime fechaHora;

    // OPCIONAL: solo se usa al finalizar()
    @DecimalMin(value = "0.000", message = "Los km recorridos no pueden ser negativos")
    private BigDecimal kmRecorridos;
}
