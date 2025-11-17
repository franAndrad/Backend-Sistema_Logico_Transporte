package ar.edu.utn.frc.backend.logistica.ms_transporte.dto.deposito;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositoCreateResponseDTO {
    private Integer idDeposito;
    private String mensaje;
}