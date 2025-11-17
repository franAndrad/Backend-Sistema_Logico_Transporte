package ar.edu.utn.frc.backend.logistica.ms_transporte.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
import org.springframework.context.annotation.Bean;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Swagger / OpenAPI sin auth
                        .requestMatchers(
                                "/transporte/swagger-ui/**",
                                "/transporte/swagger-ui.html",
                                "/transporte/v3/api-docs/**",
                                "/transporte/v3/api-docs"
                        ).permitAll()

                        // TODO: acá el resto de tus endpoints protegidos
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
