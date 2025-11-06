package ar.edu.utn.frc.backend.logistica.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RouteConfig {
   
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(spec -> spec
                        .path("/api/v1/clientes/**", "/api/v1/contenedores/**", "/api/v1/solicitudes/**")
                        .uri("http://ms-cliente:8080"))

                // .route(spec -> spec
                //         .path("/api/v1/rutas/**", "/api/v1/tramos/**", "/api/v1/tarifas/**", "/api/v1/camiones/**", "/api/v1/depositos/**", "/api/v1/transportes/**")
                //         .uri("http://localhost:8082"))
                .build();
    }
}
