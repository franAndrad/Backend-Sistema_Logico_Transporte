package ar.edu.utn.frc.backend.logistica.ms_cliente.client.transporte.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TramoDto {
    private Integer idTramo;
    private BigDecimal distancia;
    private BigDecimal costoAproximado;
    private String estado;
}

