package ar.edu.utn.frc.backend.logistica.ms_cliente.config;

import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "MS Cliente - API",
                version = "v1",
                description = "Gestión de clientes, contenedores y solicitudes"
        )
)
public class OpenApiConfig {
}