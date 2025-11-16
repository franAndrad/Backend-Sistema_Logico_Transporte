package ar.edu.utn.frc.backend.logistica.ms_cliente.client.transporte;

import ar.edu.utn.frc.backend.logistica.ms_cliente.client.transporte.dto.RutaCreateRequestDto;
import ar.edu.utn.frc.backend.logistica.ms_cliente.client.transporte.dto.RutaCreateResponseDto;
import ar.edu.utn.frc.backend.logistica.ms_cliente.client.transporte.dto.RutaDto;
import ar.edu.utn.frc.backend.logistica.ms_cliente.client.transporte.dto.TramoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@FeignClient(name = "ms-transporte", url = "${transporte.api.url:http://ms-transporte:8080}")
public interface TransporteClient {

    @PostMapping("/api/v1/rutas")
    RutaCreateResponseDto crearRuta(@RequestBody RutaCreateRequestDto body);

    @GetMapping("/api/v1/rutas/solicitud/{solicitudId}")
    RutaDto obtenerRutaPorSolicitud(@PathVariable("solicitudId") Integer solicitudId);

    @GetMapping("/api/v1/tramos/ruta/{rutaId}")
    List<TramoDto> obtenerTramosPorRuta(@PathVariable("rutaId") Integer rutaId);
}
