package ar.edu.utn.frc.backend.logistica.ms_transporte.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutasResponse {
    private Integer totalRutas;
    private RutaAlternativaDTO rutaMasRapida;
    private List<RutaAlternativaDTO> todasLasRutas;
}
