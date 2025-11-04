package ar.edu.utn.frc.backend.logistica.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
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
            .authorizeExchange(exchanges -> exchanges

                // ========== RUTAS PRUEBA ==========
                .pathMatchers(HttpMethod.GET, "/api/v1/clientes/health").hasRole("CLIENTE")
                .pathMatchers(HttpMethod.GET, "/api/v1/transportes/health").hasRole("OPERADOR")

                // ========== RUTAS PÚBLICAS (sin autenticación) ==========
                // .pathMatchers(HttpMethod.POST, "/api/v1/clientes").permitAll()  
                
                // // ========== MS-CLIENTE: Gestión de Clientes ==========
                // .pathMatchers(HttpMethod.GET, "/api/v1/clientes").hasAnyRole("OPERADOR", "ADMIN")
                // .pathMatchers(HttpMethod.GET, "/api/v1/clientes/{id}").hasAnyRole("CLIENTE", "OPERADOR", "ADMIN")
                // .pathMatchers(HttpMethod.PUT, "/api/v1/clientes/{id}").hasAnyRole("CLIENTE", "OPERADOR", "ADMIN")
                // .pathMatchers(HttpMethod.DELETE, "/api/v1/clientes/{id}").hasRole("ADMIN")
                
                // // ========== MS-CLIENTE: Gestión de Contenedores ==========
                // .pathMatchers(HttpMethod.GET, "/api/v1/contenedores").hasAnyRole("OPERADOR", "ADMIN")
                // .pathMatchers(HttpMethod.GET, "/api/v1/contenedores/**").hasAnyRole("CLIENTE", "OPERADOR", "ADMIN")
                // .pathMatchers(HttpMethod.POST, "/api/v1/contenedores").hasAnyRole("CLIENTE", "OPERADOR", "ADMIN")
                // .pathMatchers(HttpMethod.PUT, "/api/v1/contenedores/**").hasAnyRole("CLIENTE", "OPERADOR", "ADMIN")
                
                // // ========== MS-CLIENTE: Solicitudes de Transporte ==========
                // .pathMatchers(HttpMethod.GET, "/api/v1/solicitudes").hasAnyRole("OPERADOR", "ADMIN")
                // .pathMatchers(HttpMethod.GET, "/api/v1/solicitudes/**").hasAnyRole("CLIENTE", "OPERADOR", "TRANSPORTISTA", "ADMIN")
                // .pathMatchers(HttpMethod.POST, "/api/v1/solicitudes").hasRole("CLIENTE")
                // .pathMatchers(HttpMethod.PUT, "/api/v1/solicitudes/**").hasAnyRole("OPERADOR", "TRANSPORTISTA", "ADMIN")
                
                // // ========== MS-TRANSPORTE: Rutas ==========
                // .pathMatchers(HttpMethod.GET, "/api/v1/rutas").hasAnyRole("OPERADOR", "ADMIN")
                // .pathMatchers(HttpMethod.GET, "/api/v1/rutas/**").hasAnyRole("OPERADOR", "CLIENTE", "ADMIN")
                // .pathMatchers(HttpMethod.POST, "/api/v1/rutas").hasRole("OPERADOR")
                
                // // ========== MS-TRANSPORTE: Tramos ==========
                // .pathMatchers(HttpMethod.GET, "/api/v1/tramos").hasRole("OPERADOR")
                // .pathMatchers(HttpMethod.GET, "/api/v1/tramos/**").hasAnyRole("OPERADOR", "TRANSPORTISTA", "ADMIN")
                // .pathMatchers(HttpMethod.POST, "/api/v1/tramos").hasRole("OPERADOR")
                // .pathMatchers(HttpMethod.PUT, "/api/v1/tramos/{id}").hasRole("OPERADOR")
                // .pathMatchers(HttpMethod.DELETE, "/api/v1/tramos/{id}").hasRole("OPERADOR")
                // .pathMatchers(HttpMethod.POST, "/api/v1/tramos/*/iniciar").hasRole("TRANSPORTISTA")
                // .pathMatchers(HttpMethod.POST, "/api/v1/tramos/*/finalizar").hasRole("TRANSPORTISTA")
                
                // // ========== MS-TRANSPORTE: Camiones ==========
                // .pathMatchers(HttpMethod.GET, "/api/v1/camiones/**").hasAnyRole("OPERADOR", "ADMIN")
                // .pathMatchers(HttpMethod.POST, "/api/v1/camiones").hasRole("OPERADOR")
                // .pathMatchers(HttpMethod.PUT, "/api/v1/camiones/**").hasAnyRole("OPERADOR", "ADMIN")
                // .pathMatchers(HttpMethod.DELETE, "/api/v1/camiones/**").hasRole("ADMIN")
                
                // // ========== MS-TRANSPORTE: Depósitos ==========
                // .pathMatchers(HttpMethod.GET, "/api/v1/depositos/**").hasAnyRole("OPERADOR", "ADMIN")
                // .pathMatchers(HttpMethod.POST, "/api/v1/depositos").hasRole("OPERADOR")
                // .pathMatchers(HttpMethod.PUT, "/api/v1/depositos/**").hasAnyRole("OPERADOR", "ADMIN")
                // .pathMatchers(HttpMethod.DELETE, "/api/v1/depositos/**").hasRole("ADMIN")
                
                // // ========== MS-TRANSPORTE: Tarifas ==========
                // .pathMatchers(HttpMethod.GET, "/api/v1/tarifas/**").hasAnyRole("CLIENTE", "OPERADOR", "ADMIN")
                // .pathMatchers(HttpMethod.POST, "/api/v1/tarifas").hasRole("ADMIN")
                // .pathMatchers(HttpMethod.PUT, "/api/v1/tarifas/**").hasRole("ADMIN")
                // .pathMatchers(HttpMethod.DELETE, "/api/v1/tarifas/**").hasRole("ADMIN")
                
                // ========== Cualquier otra ruta requiere autenticación ==========
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
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Obtener el claim "realm_access" que contiene los roles de Keycloak
            Map<String, List<String>> realmAccess = jwt.getClaim("realm_access");
            
            if (realmAccess == null || realmAccess.get("roles") == null) {
                return List.of();
            }
            
            // Convertir cada rol al formato ROLE_XXXX esperado por Spring Security
            return realmAccess.get("roles")
                .stream()
                .map(role -> String.format("ROLE_%s", role.toUpperCase()))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        });
        
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }
}
