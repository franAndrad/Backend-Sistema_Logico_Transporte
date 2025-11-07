package ar.edu.utn.frc.backend.logistica.ms_transporte.dto.deposito;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositoCercanoResponseDTO {
    private Integer id;
    private String mensaje;
    private Double distanciaKm;
}
