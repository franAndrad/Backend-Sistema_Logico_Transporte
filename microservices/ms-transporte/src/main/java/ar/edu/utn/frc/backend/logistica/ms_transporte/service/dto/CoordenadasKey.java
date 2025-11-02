package ar.edu.utn.frc.backend.logistica.ms_transporte.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoordenadasKey implements Serializable {
    private Double origenLat;
    private Double origenLng;
    private Double destinoLat;
    private Double destinoLng;
}
