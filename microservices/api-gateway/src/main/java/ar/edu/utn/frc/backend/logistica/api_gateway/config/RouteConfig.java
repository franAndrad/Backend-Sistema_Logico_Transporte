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

                // Keycloak: Autenticacion (UI) + Token
                .route("kc-auth", s -> s.path("/auth")
                        .filters(f -> f.rewritePath("/auth","/realms/logistica/protocol/openid-connect/auth"))
                        .uri("http://keycloak:8080"))
                .route("kc-token", s -> s.path("/token")
                        .filters(f -> f.rewritePath("/token","/realms/logistica/protocol/openid-connect/token"))
                        .uri("http://keycloak:8080"))
                .route("kc-discovery", s -> s
                        .path("/realms/logistica/.well-known/openid-configuration")
                        .uri("http://keycloak:8080"))
                .route("kc-realm-all", s -> s
                        .path("/realms/logistica/**", "/resources/**", "/js/**")
                        .uri("http://keycloak:8080"))

                // ms-cliente
                .route(spec -> spec
                        .path("/api/v1/clientes/**", "/api/v1/contenedores/**", "/api/v1/solicitudes/**")
                        .uri("http://ms-cliente:8080"))

                // ms-transporte
                .route(spec -> spec
                        .path("/api/v1/rutas/**","/api/v1/distancia/**", "/api/v1/tramos/**", "/api/v1/tarifas/**", "/api/v1/camiones/**", "/api/v1/depositos/**", "/api/v1/transportes/**")
                        .uri("http://ms-transporte:8080"))


                //   SWAGGER ms-cliente

                .route("ms-cliente-swagger", r -> r
                        .path(
                                "/cliente/swagger-ui/**",
                                "/cliente/swagger-ui.html",
                                "/cliente/v3/api-docs/**",
                                "/cliente/v3/api-docs"
                        )

                        .uri("http://ms-cliente:8080"))


                //   SWAGGER ms-cliente


                .route("ms-transporte-swagger", r -> r
                        .path(
                                "/transporte/swagger-ui/**",
                                "/transporte/swagger-ui.html",
                                "/transporte/v3/api-docs/**",
                                "/transporte/v3/api-docs"
                        )
                        .uri("http://ms-transporte:8080"))

                .build();
    }
}
