package ar.edu.utn.frc.backend.logistica.ms_transporte.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.cloud.openfeign.FeignClient;
import java.util.Map;

@FeignClient(name = "ms-cliente", url = "${cliente.api.url:http://ms-cliente:8080}")
public interface ClienteClient {

    @PutMapping("/api/v1/solicitudes/{id}/estado")
    void actualizarEstado(@PathVariable("id") Integer idSolicitud,
                          @RequestBody Map<String, String> body);
}
