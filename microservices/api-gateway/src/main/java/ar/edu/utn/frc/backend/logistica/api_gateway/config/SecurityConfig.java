package ar.edu.utn.frc.backend.logistica.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .authorizeExchange(ex -> ex
                .pathMatchers("/auth", "/token").permitAll()
                .pathMatchers("/realms/logistica/.well-known/openid-configuration").permitAll()
                .pathMatchers("/realms/logistica/**", "/resources/**", "/js/**").permitAll()

                .pathMatchers(HttpMethod.GET, "/api/v1/clientes/health").hasRole("CLIENTE")

                .pathMatchers(HttpMethod.GET, "/api/v1/transportes/health").hasRole("OPERADOR")

                .pathMatchers(HttpMethod.GET, "/api/v1/distancia").hasRole("CLIENTE")

                // ===================== Cliente =====================
                .pathMatchers(HttpMethod.PUT, "/api/v1/clientes/*").hasAnyRole("CLIENTE", "OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.DELETE, "/api/v1/clientes/*").hasRole("ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/v1/clientes/*").hasAnyRole("OPERADOR", "CLIENTE", "ADMIN")
                .pathMatchers(HttpMethod.POST, "/api/v1/clientes").hasAnyRole("OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/v1/clientes").hasAnyRole("OPERADOR", "ADMIN")

                // ===================== Contenedor =====================
                .pathMatchers(HttpMethod.GET, "/api/v1/contenedores/cliente/*").hasAnyRole("CLIENTE", "OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.PUT, "/api/v1/contenedores/*").hasAnyRole("CLIENTE", "OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.DELETE, "/api/v1/contenedores/*").hasRole("ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/v1/contenedores/*").hasAnyRole("CLIENTE", "OPERADOR", "ADMIN")             
                .pathMatchers(HttpMethod.POST, "/api/v1/contenedores").hasAnyRole("CLIENTE", "OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/v1/contenedores").hasAnyRole("OPERADOR", "ADMIN")

                // ===================== Solicitud =====================
                .pathMatchers(HttpMethod.GET, "/api/v1/solicitudes/*/estado").hasAnyRole("CLIENTE", "OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/v1/solicitudes/cliente/*").hasAnyRole("CLIENTE", "OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.PUT, "/api/v1/solicitudes/*/estado").hasAnyRole("TRANSPORTISTA", "OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.PUT, "/api/v1/solicitudes/*").hasAnyRole("OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/v1/solicitudes/*").hasAnyRole("CLIENTE", "OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.POST, "/api/v1/solicitudes").hasAnyRole("CLIENTE", "ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/v1/solicitudes").hasAnyRole("OPERADOR", "ADMIN")

                // ===================== Ruta =====================
                .pathMatchers(HttpMethod.GET, "/api/v1/rutas/solicitud/*").hasAnyRole("OPERADOR", "CLIENTE")
                .pathMatchers(HttpMethod.DELETE, "/api/v1/rutas/*").hasRole("OPERADOR")
                .pathMatchers(HttpMethod.GET, "/api/v1/rutas/*").hasRole("OPERADOR")
                .pathMatchers(HttpMethod.POST, "/api/v1/rutas").hasRole("OPERADOR")
                .pathMatchers(HttpMethod.GET, "/api/v1/rutas").hasAnyRole("OPERADOR", "ADMIN")

                // ===================== Tramo =====================
                .pathMatchers(HttpMethod.POST, "/api/v1/tramos/*/iniciar").hasRole("TRANSPORTISTA")
                .pathMatchers(HttpMethod.POST, "/api/v1/tramos/*/finalizar").hasRole("TRANSPORTISTA")
                .pathMatchers(HttpMethod.PUT, "/api/v1/tramos/*/camion").hasRole("OPERADOR")
                .pathMatchers(HttpMethod.DELETE, "/api/v1/tramos/*/camion").hasRole("OPERADOR")
                .pathMatchers(HttpMethod.GET, "/api/v1/tramos/ruta/*").hasRole("OPERADOR")
                .pathMatchers(HttpMethod.PUT, "/api/v1/tramos/*").hasRole("OPERADOR")
                .pathMatchers(HttpMethod.GET, "/api/v1/tramos/*").hasAnyRole("OPERADOR", "TRANSPORTISTA")
                .pathMatchers(HttpMethod.GET, "/api/v1/tramos").hasRole("OPERADOR")

                // ===================== Depositos =====================
                .pathMatchers(HttpMethod.PATCH, "/api/v1/depositos/*/activar").hasRole("ADMIN")
                .pathMatchers(HttpMethod.PATCH, "/api/v1/depositos/*/desactivar").hasRole("ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/v1/depositos/cercanos").hasAnyRole("OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/v1/depositos/activos").hasAnyRole("OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/v1/depositos/*").hasAnyRole("OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.POST, "/api/v1/depositos").hasAnyRole("OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.PUT, "/api/v1/depositos").hasAnyRole("OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/v1/depositos").hasAnyRole("OPERADOR", "ADMIN")

                // ===================== Camiones =====================
                .pathMatchers(HttpMethod.PATCH, "/api/v1/camiones/*/habilitar").hasRole("ADMIN")
                .pathMatchers(HttpMethod.PATCH, "/api/v1/camiones/*/deshabilitar").hasRole("ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/v1/camiones/disponibles").hasAnyRole("OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.PUT, "/api/v1/camiones/*").hasAnyRole("OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/v1/camiones/*").hasAnyRole("OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.POST, "/api/v1/camiones").hasAnyRole("OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/v1/camiones").hasAnyRole("OPERADOR", "ADMIN")

                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor()))
            )
            .csrf(ServerHttpSecurity.CsrfSpec::disable);

        return http.build();
    }

    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
        JwtAuthenticationConverter conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, List<String>> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess == null || realmAccess.get("roles") == null) return List.of();
            return realmAccess.get("roles").stream()
                .map(r -> "ROLE_" + r.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        });
        return new ReactiveJwtAuthenticationConverterAdapter(conv);
    }
}
