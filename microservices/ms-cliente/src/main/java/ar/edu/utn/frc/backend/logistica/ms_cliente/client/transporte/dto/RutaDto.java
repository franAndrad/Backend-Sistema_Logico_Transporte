package ar.edu.utn.frc.backend.logistica.ms_cliente.client.transporte.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutaDto {
    private Integer idRuta;
    private Integer idSolicitud;
    private Integer cantidadTramos;
    private Integer cantidadDepositos;
    private BigDecimal distanciaTotal;
    private String estado;
}

