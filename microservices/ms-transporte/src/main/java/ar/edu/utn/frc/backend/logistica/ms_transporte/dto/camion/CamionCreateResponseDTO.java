package ar.edu.utn.frc.backend.logistica.ms_transporte.dto.camion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CamionCreateResponseDTO {
    private String dominio;
    private String mensaje;
}