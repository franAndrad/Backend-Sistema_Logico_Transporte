package ar.edu.utn.frc.backend.logistica.ms_transporte.config;

import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "MS Transporte - API",
                version = "v1",
                description = "Gestión de viajes, recorridos y asignación de transportes"
        )
)
public class OpenApiConfig {
}