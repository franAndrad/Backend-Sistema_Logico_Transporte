package ar.edu.utn.frc.backend.logistica.ms_transporte.dto.ruta;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutaResponseDTO {
    private Integer idRuta;
    private String mensaje;
}
