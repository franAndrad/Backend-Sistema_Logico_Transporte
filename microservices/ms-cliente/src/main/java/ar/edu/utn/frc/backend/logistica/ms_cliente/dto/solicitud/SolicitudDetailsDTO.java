package ar.edu.utn.frc.backend.logistica.ms_cliente.dto.solicitud;

import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.SolicitudEstado;
import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.cliente.ClienteDetailsDTO;
import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.contenedor.ContenedorSummaryDTO;
import ar.edu.utn.frc.backend.logistica.ms_cliente.client.transporte.dto.RutaDto;
import ar.edu.utn.frc.backend.logistica.ms_cliente.client.transporte.dto.TramoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudDetailsDTO {
    private Integer id;
    private String origenDireccion;
    private String destinoDireccion;
    private SolicitudEstado estado;
    private Double costoEstimado;
    private Integer tiempoEstimado;
    private String contenedorIdentificacion;
    private ClienteDetailsDTO cliente;
    private ContenedorSummaryDTO contenedor;
    private RutaDto ruta;
    private List<TramoDto> tramos;
    private Double costoFinal;
    private Integer tiempoReal;
}
