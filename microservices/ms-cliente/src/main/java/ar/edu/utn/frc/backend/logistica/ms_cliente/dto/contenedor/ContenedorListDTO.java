package ar.edu.utn.frc.backend.logistica.ms_cliente.dto.contenedor;

import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.ContenedorEstado;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContenedorListDTO {
    private Integer id;
    private String identificacion;
    private Double peso;
    private Double volumen;
    private ContenedorEstado estado;
    private Integer clienteId;
}

