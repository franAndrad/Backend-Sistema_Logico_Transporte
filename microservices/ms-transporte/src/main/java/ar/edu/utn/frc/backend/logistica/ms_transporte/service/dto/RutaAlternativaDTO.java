package ar.edu.utn.frc.backend.logistica.ms_transporte.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutaAlternativaDTO {
    private Integer numeroRuta;
    private Double distanciaKm;
    private Long duracionMinutos;
    private String resumen;
    private Boolean esMasRapida;
}
