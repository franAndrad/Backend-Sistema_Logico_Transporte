package ar.edu.utn.frc.backend.logistica.ms_cliente.dto.contenedor;

import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.ContenedorEstado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContenedorCreateDTO {

    @NotBlank(message = "La identificación es obligatoria")
    private String identificacion;

    @NotNull(message = "El id del cliente es obligatorio")
    private Integer clienteId;

    @NotNull(message = "El peso es obligatorio")
    private Double peso;

    @NotNull(message = "El volumen es obligatorio")
    private Double volumen;

    private ContenedorEstado estado; // opcional, por defecto EN_ORIGEN
}

