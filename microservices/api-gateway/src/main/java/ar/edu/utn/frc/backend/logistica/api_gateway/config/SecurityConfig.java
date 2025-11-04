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
