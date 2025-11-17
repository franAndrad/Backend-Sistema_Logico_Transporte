package ar.edu.utn.frc.backend.logistica.ms_transporte.dto.camion;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CamionCreateResponseDTO {
    private String dominio;
    private String mensaje;
}