package ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto;

import lombok.Data;
import java.util.List;

@Data
public class DirectionsResponse {
    
    private String status;
    private List<Route> routes;
    
    @Data
    public static class Route {
        private List<Leg> legs;
    }
    
    @Data
    public static class Leg {
        private Distance distance;
        private Duration duration;
    }
    
    @Data
    public static class Distance {
        private Long value;
    }
    
    @Data
    public static class Duration {
        private Long value;
    }
}
