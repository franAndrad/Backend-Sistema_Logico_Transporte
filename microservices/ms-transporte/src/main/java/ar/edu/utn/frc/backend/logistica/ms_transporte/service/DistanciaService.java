package ar.edu.utn.frc.backend.logistica.ms_transporte.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.client.GoogleMapsClient;
import ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto.DirectionsResponse;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.dto.CoordenadasKey;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.dto.RutaAlternativaDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.dto.RutasResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.Locale;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistanciaService {
    
    private final GoogleMapsClient googleMapsClient;
    
    @Value("${google.maps.api.key}")
    private String apiKey;
    
    @Value("${google.maps.api.url}")
    private String apiUrl;
    
    /**
     * Obtiene todas las rutas y las CACHEA en memoria por 15 minutos
     * La segunda vez que se llame con las mismas coordenadas, no llamará a Google Maps
     */
    @Cacheable(value = "rutasAlternativas", key = "#coordenadas")
    public RutasResponse obtenerTodasLasRutas(CoordenadasKey coordenadas) {
        System.out.println("🌐 LLAMANDO A GOOGLE MAPS API (no está en caché)");
        
        String origin = String.format(Locale.US, "%.6f,%.6f", 
            coordenadas.getOrigenLat(), coordenadas.getOrigenLng());
        String destination = String.format(Locale.US, "%.6f,%.6f", 
            coordenadas.getDestinoLat(), coordenadas.getDestinoLng());

        System.out.println("=================================================");
        System.out.println("🗺️  OBTENIENDO TODAS LAS RUTAS ALTERNATIVAS");
        System.out.println("📍 Origen: " + origin);
        System.out.println("📍 Destino: " + destination);
        System.out.println("=================================================");

        try {
            DirectionsResponse response = googleMapsClient.getDirections(
                origin, 
                destination,
                "driving",
                true,  // alternatives=true para obtener rutas alternativas
                apiKey
            );

            if (!"OK".equals(response.getStatus())) {
                throw new RuntimeException("Error al obtener rutas: " + response.getStatus());
            }
            
            if (response.getRoutes() == null || response.getRoutes().isEmpty()) {
                throw new RuntimeException("No se encontraron rutas");
            }

            // Convertir todas las rutas a DTOs
            List<RutaAlternativaDTO> rutas = IntStream.range(0, response.getRoutes().size())
                .mapToObj(i -> {
                    var route = response.getRoutes().get(i);
                    var leg = route.getLegs().get(0);
                    
                    double distanciaKm = leg.getDistance().getValue() / 1000.0;
                    long duracionMinutos = leg.getDuration().getValue() / 60;
                    
                    return new RutaAlternativaDTO(
                        i + 1,  // Número de ruta (1, 2, 3...)
                        Math.round(distanciaKm * 100.0) / 100.0,  // 2 decimales
                        duracionMinutos,
                        route.getSummary() != null ? route.getSummary() : "Ruta " + (i + 1),
                        false  // Se marcará después
                    );
                })
                .toList();

            // Encontrar la ruta más rápida
            RutaAlternativaDTO rutaMasRapida = rutas.stream()
                .min((r1, r2) -> Long.compare(r1.getDuracionMinutos(), r2.getDuracionMinutos()))
                .orElseThrow(() -> new RuntimeException("No se pudo determinar la ruta más rápida"));
            
            rutaMasRapida.setEsMasRapida(true);

            System.out.println("✅ Se encontraron " + rutas.size() + " rutas alternativas");
            System.out.println("🏆 Ruta más rápida: #" + rutaMasRapida.getNumeroRuta() + 
                             " (" + rutaMasRapida.getDuracionMinutos() + " minutos)");
            System.out.println("� Guardando en CACHÉ por 15 minutos");
            System.out.println("=================================================");

            return new RutasResponse(rutas.size(), rutaMasRapida, rutas);
            
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("💥 Error al obtener rutas:");
            e.printStackTrace();
            throw new RuntimeException("Error al obtener rutas: " + e.getMessage(), e);
        }
    }
    
    /**
     * Sobrecarga del método para mantener compatibilidad
     */
    public RutasResponse obtenerTodasLasRutas(
        Double origenLat, Double origenLng,
        Double destinoLat, Double destinoLng
    ) {
        CoordenadasKey key = new CoordenadasKey(origenLat, origenLng, destinoLat, destinoLng);
        return obtenerTodasLasRutas(key);
    }
    
    public Double calcularDistanciaKm(
        Double origenLat, 
        Double origenLng,
        Double destinoLat, 
        Double destinoLng
    ) {
        // Reutiliza el método cacheado
        RutasResponse rutas = obtenerTodasLasRutas(origenLat, origenLng, destinoLat, destinoLng);
        return rutas.getRutaMasRapida().getDistanciaKm();
    }
    
    public Long calcularDuracionMinutos(
        Double origenLat, 
        Double origenLng,
        Double destinoLat, 
        Double destinoLng
    ) {
        // Reutiliza el método cacheado
        RutasResponse rutas = obtenerTodasLasRutas(origenLat, origenLng, destinoLat, destinoLng);
        return rutas.getRutaMasRapida().getDuracionMinutos();
    }
}