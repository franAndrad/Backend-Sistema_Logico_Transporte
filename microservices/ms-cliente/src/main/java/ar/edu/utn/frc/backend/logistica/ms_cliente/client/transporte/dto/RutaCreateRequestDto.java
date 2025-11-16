package ar.edu.utn.frc.backend.logistica.ms_cliente.client.transporte.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutaCreateRequestDto {
    private Integer idSolicitud;
    private BigDecimal origenLat;
    private BigDecimal origenLon;
    private BigDecimal destinoLat;
    private BigDecimal destinoLon;
    private List<Integer> depositoIds;
}

