package ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
        private Long value;  // Metros
    }
    
    @Data
    public static class Duration {
        private Long value;  // Segundos
    }
    
    // Utilities
    public Long getDistanceInMeters() {
        if (routes != null && !routes.isEmpty() 
            && routes.get(0).getLegs() != null 
            && !routes.get(0).getLegs().isEmpty()) {
            return routes.get(0).getLegs().get(0).getDistance().getValue();
        }
        return null;
    }
    
    public Double getDistanceInKilometers() {
        Long meters = getDistanceInMeters();
        return meters != null ? meters / 1000.0 : null;
    }
    
    public Long getDurationInSeconds() {
        if (routes != null && !routes.isEmpty() 
            && routes.get(0).getLegs() != null 
            && !routes.get(0).getLegs().isEmpty()) {
            return routes.get(0).getLegs().get(0).getDuration().getValue();
        }
        return null;
    }
    
    public boolean isSuccess() {
        return "OK".equals(status);
    }
}
