package ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tramo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TramoAsignarCamionRequestDTO {
    @NotBlank(message = "El dominio es obligatorio")
    @Size(max = 20, message = "El dominio no debe superar 20 caracteres")
    private String dominio;
}
