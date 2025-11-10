package ar.edu.utn.frc.backend.logistica.ms_cliente.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClienteListDTO {
    private Integer id;
    private String nombre;
    private String email;
    private String telefono;
    private Boolean activo;
}