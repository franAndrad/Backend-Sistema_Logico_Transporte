package ar.edu.utn.frc.backend.logistica.ms_cliente.dto.contenedor;

import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.ContenedorEstado;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContenedorUpdateDTO {

    @NotNull(message = "El peso es obligatorio")
    private Double peso;

    @NotNull(message = "El volumen es obligatorio")
    private Double volumen;

    @NotNull(message = "El estado es obligatorio")
    private ContenedorEstado estado;
}

