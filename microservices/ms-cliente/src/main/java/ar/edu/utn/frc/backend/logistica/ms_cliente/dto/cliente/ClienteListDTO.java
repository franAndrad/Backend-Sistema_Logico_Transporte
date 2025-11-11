package ar.edu.utn.frc.backend.logistica.ms_cliente.dto.cliente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClienteListDTO {
    private Integer id;
    private String keycloakId;
    private String razonSocial;
    private String cuit;
    private Boolean activo;
}