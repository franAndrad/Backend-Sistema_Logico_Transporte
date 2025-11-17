package ar.edu.utn.frc.backend.logistica.ms_cliente.dto.cliente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClienteUpdateDTO {

    @NotBlank(message = "La dirección de facturación es obligatoria")
    private String direccionFacturacion;

    private String direccionEnvio;

    private String razonSocial;

    @NotBlank(message = "El CUIT no puede ser nulo")
    @Size(min = 11, max = 11, message = "El CUIT debe tener 11 dígitos")
    private String cuit;
}