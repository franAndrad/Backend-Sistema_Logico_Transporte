package ar.edu.utn.frc.backend.logistica.ms_transporte.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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
