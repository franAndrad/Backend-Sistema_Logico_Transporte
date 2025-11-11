package ar.edu.utn.frc.backend.logistica.ms_cliente.dto.cliente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClienteCreateDTO {
    @NotBlank(message = "El keycloakId es obligatorio")
    private String keycloakId;

    @NotBlank(message = "La dirección de facturación es obligatoria")
    private String direccionFacturacion;

    private String direccionEnvio;

    private String razonSocial;

    // CUIT obligatorio: exactamente 11 dígitos numéricos
    @NotBlank(message = "El CUIT no puede ser nulo")
    @Size(min = 11, max = 11, message = "El CUIT debe tener exactamente 11 dígitos")
    @Pattern(regexp = "\\d{11}", message = "El CUIT debe contener solo dígitos (11)")
    private String cuit;
}
