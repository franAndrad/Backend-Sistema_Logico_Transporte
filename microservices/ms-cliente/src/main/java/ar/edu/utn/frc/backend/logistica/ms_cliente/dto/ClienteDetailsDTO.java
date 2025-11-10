package ar.edu.utn.frc.backend.logistica.ms_cliente.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClienteDetailsDTO {
    private Integer id;
    private String nombre;
    private String apellido;
    private String email;
    private String direccionFacturacion;
    private String razonSocial;
    private String cuit;
}