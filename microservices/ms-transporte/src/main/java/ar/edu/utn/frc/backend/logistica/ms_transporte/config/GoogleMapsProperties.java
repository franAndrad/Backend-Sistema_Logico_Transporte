package ar.edu.utn.frc.backend.logistica.ms_transporte.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "google.maps")
public class GoogleMapsProperties {

    private Api api = new Api();
    private String apiUrl;

    @Data
    public static class Api {
        private String key;
        private String url; 
    }
}
