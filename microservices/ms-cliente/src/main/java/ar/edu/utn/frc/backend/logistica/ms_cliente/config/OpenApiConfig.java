package ar.edu.utn.frc.backend.logistica.ms_cliente.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

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