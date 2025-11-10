package ar.edu.utn.frc.backend.logistica.ms_cliente.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClienteResponseDTO {
    private Integer idCliente;
    private String mensaje;
}