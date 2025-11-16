package ar.edu.utn.frc.backend.logistica.ms_cliente.client.transporte.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutaCreateResponseDto {
    private Integer idRuta;
    private String mensaje;
}

