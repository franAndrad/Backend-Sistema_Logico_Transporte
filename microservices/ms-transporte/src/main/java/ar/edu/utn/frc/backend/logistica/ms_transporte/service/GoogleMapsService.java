package ar.edu.utn.frc.backend.logistica.ms_transporte.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.client.GoogleMapsClient;
import ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto.DirectionsResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.config.GoogleMapsProperties;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.ruta.RutaCalculadaDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.distancia.DistanciaConTramosResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.distancia.DistanciaResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.ruta.LegCalculadoDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Deposito;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleMapsService {

    private final GoogleMapsClient googleMapsClient;
    private final GoogleMapsProperties googleMapsProperties;
    private final DepositoService depositoService;

    public DistanciaResponseDTO calcularDistancia( Double origenLat, Double origenLng,
            Double destinoLat, Double destinoLng, 
            List<Integer> depositoIds
    ) {

        final String origin = origenLat + "," + origenLng;
        final String destination = destinoLat + "," + destinoLng;
        final String waypoints = buildWaypointsFromSelectedDepositos(depositoIds);

        DirectionsResponseDTO response = googleMapsClient.getDirections(
                origin,
                destination,
                "driving",
                false,
                waypoints,
                googleMapsProperties.getApi().getKey());

        if (response == null || !"OK".equals(response.getStatus())) {
            throw new RuntimeException(
                    "Error en Google Maps API: " + (response != null ? response.getStatus() : "null"));
        }
        if (response.getRoutes() == null || response.getRoutes().isEmpty()
                || response.getRoutes().get(0).getLegs() == null
                || response.getRoutes().get(0).getLegs().isEmpty()) {
            throw new RuntimeException("Google Maps API devolvió sin rutas/legs");
        }

        DirectionsResponseDTO.Route route = response.getRoutes().get(0);

        long totalMeters = 0L;
        long totalSeconds = 0L;

        for (DirectionsResponseDTO.Leg l : route.getLegs()) {
            if (l.getDistance() == null || l.getDistance().getValue() == null) {
                throw new RuntimeException("Leg sin distancia en respuesta de Google Maps");
            }
            if (l.getDuration() == null || l.getDuration().getValue() == null) {
                throw new RuntimeException("Leg sin duración en respuesta de Google Maps");
            }
            totalMeters += l.getDistance().getValue();
            totalSeconds += l.getDuration().getValue();
        }

        double distanciaKm = totalMeters / 1000.0;
        long duracionMinutos = totalSeconds / 60;

        return new DistanciaResponseDTO(
                origenLat, origenLng,
                destinoLat, destinoLng,
                distanciaKm, duracionMinutos);
    }

    
    public DistanciaConTramosResponseDTO calcularDistanciaConTramos(
            Double origenLat, Double origenLng,
            Double destinoLat, Double destinoLng,
            List<Integer> depositoIds
    ) {
        final String origin = origenLat + "," + origenLng;
        final String destination = destinoLat + "," + destinoLng;
        final String waypoints = buildWaypointsFromSelectedDepositos(depositoIds);

        DirectionsResponseDTO response = googleMapsClient.getDirections(
                origin, destination, "driving", false, waypoints, googleMapsProperties.getApi().getKey());

        if (response == null || !"OK".equals(response.getStatus())) {
            throw new RuntimeException("Error en Google Maps API: " + (response != null ? response.getStatus() : "null"));
        }
        var route = response.getRoutes().get(0);
        var legs = route.getLegs();
        if (legs == null || legs.isEmpty()) {
            throw new RuntimeException("Google Maps API devolvió sin rutas/legs");
        }

        long totalMeters = 0L;
        long totalSeconds = 0L;

        List<Integer> deposOrdenados = (depositoIds == null)
                ? List.of()
                : List.copyOf(new LinkedHashSet<>(depositoIds));
        int puntos = deposOrdenados.size() + 2; 

        List<LegCalculadoDTO> legDtos = new ArrayList<>();

        for (int i = 0; i < legs.size(); i++) {
            var l = legs.get(i);
            long meters = l.getDistance().getValue();
            long seconds = l.getDuration().getValue();
            totalMeters += meters;
            totalSeconds += seconds;

            Integer depOri = null, depDes = null;
            String tipo;

            if (deposOrdenados.isEmpty()) {
                tipo = "ORIGEN_DESTINO";
            } else if (i == 0) {
                tipo = "ORIGEN_DEPOSITO";
                depDes = deposOrdenados.get(0);
            } else if (i == puntos - 2) {
                tipo = "DEPOSITO_DESTINO";
                depOri = deposOrdenados.get(deposOrdenados.size() - 1);
            } else {
                tipo = "DEPOSITO_DEPOSITO";
                depOri = deposOrdenados.get(i - 1);
                depDes = deposOrdenados.get(i);
            }

            legDtos.add(new LegCalculadoDTO(
                    BigDecimal.valueOf(meters / 1000.0).setScale(3, RoundingMode.HALF_UP),
                    seconds / 60,
                    depOri,
                    depDes,
                    tipo
            ));
        }

        return new DistanciaConTramosResponseDTO(
                origenLat, origenLng,
                destinoLat, destinoLng,
                BigDecimal.valueOf(totalMeters / 1000.0).setScale(3, RoundingMode.HALF_UP),
                totalSeconds / 60,
                legDtos
        );
    }


    private String buildWaypointsFromSelectedDepositos(List<Integer> depositoIds) {
        if (depositoIds == null)
            return null; 
        if (depositoIds.isEmpty())
            return ""; 

        depositoIds = List.copyOf(new LinkedHashSet<>(depositoIds));

        List<Deposito> depos = depositoService.findActivosByIdsKeepingOrder(depositoIds);
        if (depos.isEmpty()) {
            throw new NoSuchElementException("No hay depósitos válidos para los ids: " + depositoIds);
        }
        if (depos.size() > 23) {
            throw new IllegalArgumentException("Se permiten hasta 23 depósitos como waypoints.");
        }

        return depos.stream()
                .map(d -> toLatLng(d.getLatitud(), d.getLongitud()))
                .collect(Collectors.joining("|"));
    }

    private String toLatLng(BigDecimal lat, BigDecimal lng) {
        return lat.stripTrailingZeros().toPlainString()
                + ","
                + lng.stripTrailingZeros().toPlainString();
    }


    public RutaCalculadaDTO calcularRutaYTramos(Double origenLat, Double origenLng, 
            Double destinoLat, Double destinoLng, 
            List<Integer> depositoIds
    ) {

    final String origin = origenLat + "," + origenLng;
    final String destination = destinoLat + "," + destinoLng;
    final String waypoints = buildWaypointsFromSelectedDepositos(depositoIds);

    DirectionsResponseDTO response = googleMapsClient.getDirections(
            origin, destination, "driving", false, waypoints, googleMapsProperties.getApi().getKey());

    if (response == null || !"OK".equals(response.getStatus())) {
        throw new RuntimeException("Error en Google Maps API: " + (response != null ? response.getStatus() : "null"));
    }
    var route = response.getRoutes().get(0);
    var legs = route.getLegs();
    if (legs == null || legs.isEmpty()) {
        throw new RuntimeException("Google Maps API devolvió sin rutas/legs");
    }

    long totalMeters = 0L;
    long totalSeconds = 0L;

    List<Integer> deposOrdenados = depositoIds == null ? List.of() : List.copyOf(new LinkedHashSet<>(depositoIds));
    int puntos = deposOrdenados.size() + 2;

    List<LegCalculadoDTO> legDtos = new ArrayList<>();

    for (int i = 0; i < legs.size(); i++) {
        var l = legs.get(i);
        long meters = l.getDistance().getValue();
        long seconds = l.getDuration().getValue();
        totalMeters += meters;
        totalSeconds += seconds;

        Integer depOri = null, depDes = null;
        String tipo;

        if (deposOrdenados.isEmpty()) {
            tipo = "ORIGEN_DESTINO";
        } else if (i == 0) {
            tipo = "ORIGEN_DEPOSITO";
            depDes = deposOrdenados.get(0);
        } else if (i == puntos - 2) {
            tipo = "DEPOSITO_DESTINO";
            depOri = deposOrdenados.get(deposOrdenados.size() - 1);
        } else {
            tipo = "DEPOSITO_DEPOSITO";
            depOri = deposOrdenados.get(i - 1);
            depDes = deposOrdenados.get(i);
        }

        legDtos.add(new LegCalculadoDTO(
                BigDecimal.valueOf(meters / 1000.0).setScale(3, RoundingMode.HALF_UP),
                seconds / 60,
                depOri,
                depDes,
                tipo
        ));
    }

    return new RutaCalculadaDTO(
            BigDecimal.valueOf(totalMeters / 1000.0).setScale(3, RoundingMode.HALF_UP),
            totalSeconds / 60,
            legDtos
    );
}
}
