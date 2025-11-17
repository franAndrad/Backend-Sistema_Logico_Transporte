package ar.edu.utn.frc.backend.logistica.ms_transporte.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.distancia.DistanciaConTramosResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.distancia.DistanciaResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.client.dto.DirectionsResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.config.GoogleMapsProperties;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.TarifaRepository;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.ruta.RutaCalculadaDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.ruta.LegCalculadoDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.client.GoogleMapsClient;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Deposito;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Tarifa;
import org.springframework.stereotype.Service;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import java.util.LinkedHashSet;
import java.math.RoundingMode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleMapsService {

    private final GoogleMapsClient googleMapsClient;
    private final GoogleMapsProperties googleMapsProperties;
    private final DepositoService depositoService;
    private final TarifaRepository tarifaRepository;
    private final CamionService camionService;

    public DistanciaResponseDTO calcularDistancia(Double origenLat, Double origenLng,
                                                  Double destinoLat, Double destinoLng,
                                                  List<Integer> depositoIds) {
        log.info("Calculando distancia de {}:{} a {}:{} con depósitos {}", origenLat, origenLng, destinoLat, destinoLng, depositoIds);

        final String origin = origenLat + "," + origenLng;
        final String destination = destinoLat + "," + destinoLng;
        final String waypoints = buildWaypointsFromSelectedDepositos(depositoIds);

        DirectionsResponseDTO response = googleMapsClient.getDirections(origin, destination, "driving", false, waypoints, googleMapsProperties.getApi().getKey());

        if (response == null || !"OK".equals(response.getStatus())) {
            log.error("Error en Google Maps API: {}", (response != null ? response.getStatus() : "null"));
            throw new RuntimeException("Error en Google Maps API: " + (response != null ? response.getStatus() : "null"));
        }

        var route = response.getRoutes().getFirst();
        if (route == null || route.getLegs() == null || route.getLegs().isEmpty()) {
            log.error("Google Maps API devolvió sin rutas/legs");
            throw new RuntimeException("Google Maps API devolvió sin rutas/legs");
        }

        long totalMeters = 0L;
        long totalSeconds = 0L;
        Tarifa tarifa = obtenerTarifaVigente();
        BigDecimal consumoProm = obtenerConsumoPromedio();
        BigDecimal costoTotal = BigDecimal.ZERO;

        for (DirectionsResponseDTO.Leg l : route.getLegs()) {
            if (l.getDistance() == null || l.getDistance().getValue() == null) {
                log.error("Leg sin distancia en respuesta de Google Maps");
                throw new RuntimeException("Leg sin distancia en respuesta de Google Maps");
            }
            if (l.getDuration() == null || l.getDuration().getValue() == null) {
                log.error("Leg sin duración en respuesta de Google Maps");
                throw new RuntimeException("Leg sin duración en respuesta de Google Maps");
            }

            long meters = l.getDistance().getValue();
            long seconds = l.getDuration().getValue();
            totalMeters += meters;
            totalSeconds += seconds;

            BigDecimal distanciaKmLeg = BigDecimal.valueOf(meters / 1000.0).setScale(3, RoundingMode.HALF_UP);
            BigDecimal costoLeg = calcularCostoAproximadoTramo(distanciaKmLeg, tarifa, consumoProm);
            costoTotal = costoTotal.add(costoLeg);
            log.debug("Leg: {} m, {} s, distanciaKm {}, costoLeg {}", meters, seconds, distanciaKmLeg, costoLeg);
        }

        double distanciaKm = totalMeters / 1000.0;
        long duracionMinutos = totalSeconds / 60;
        log.info("Distancia total {} km, duración {} min, costo total {}", distanciaKm, duracionMinutos, costoTotal);

        return new DistanciaResponseDTO(
                origenLat, origenLng,
                destinoLat, destinoLng,
                distanciaKm, duracionMinutos,
                costoTotal.setScale(2, RoundingMode.HALF_UP)
        );
    }

    public DistanciaConTramosResponseDTO calcularDistanciaConTramos(Double origenLat, Double origenLng,
                                                                    Double destinoLat, Double destinoLng,
                                                                    List<Integer> depositoIds) {
        log.info("Calculando distancia con tramos de {}:{} a {}:{} con depósitos {}", origenLat, origenLng, destinoLat, destinoLng, depositoIds);

        final String origin = origenLat + "," + origenLng;
        final String destination = destinoLat + "," + destinoLng;
        final String waypoints = buildWaypointsFromSelectedDepositos(depositoIds);

        DirectionsResponseDTO response = googleMapsClient.getDirections(origin, destination, "driving", false, waypoints, googleMapsProperties.getApi().getKey());
        if (response == null || !"OK".equals(response.getStatus())) {
            log.error("Error en Google Maps API: {}", (response != null ? response.getStatus() : "null"));
            throw new RuntimeException("Error en Google Maps API: " + (response != null ? response.getStatus() : "null"));
        }

        var route = response.getRoutes().getFirst();
        var legs = route.getLegs();
        if (legs == null || legs.isEmpty()) {
            log.error("Google Maps API devolvió sin rutas/legs");
            throw new RuntimeException("Google Maps API devolvió sin rutas/legs");
        }

        long totalMeters = 0L;
        long totalSeconds = 0L;
        List<Integer> deposOrdenados = depositoIds == null ? List.of() : List.copyOf(new LinkedHashSet<>(depositoIds));
        int puntos = deposOrdenados.size() + 2;
        List<LegCalculadoDTO> legDtos = new ArrayList<>();

        Tarifa tarifa = obtenerTarifaVigente();
        BigDecimal consumoProm = obtenerConsumoPromedio();
        BigDecimal costoTotal = BigDecimal.ZERO;

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

            BigDecimal distanciaKmLeg = BigDecimal.valueOf(meters / 1000.0).setScale(3, RoundingMode.HALF_UP);
            BigDecimal costoLeg = calcularCostoAproximadoTramo(distanciaKmLeg, tarifa, consumoProm);
            costoTotal = costoTotal.add(costoLeg);

            legDtos.add(new LegCalculadoDTO(distanciaKmLeg, seconds / 60, depOri, depDes, tipo, costoLeg.setScale(2, RoundingMode.HALF_UP)));
            log.debug("Leg {}: tipo {}, depOri {}, depDes {}, distancia {}, costo {}", i, tipo, depOri, depDes, distanciaKmLeg, costoLeg);
        }

        log.info("Distancia total {} km, duración {} min, costo total {}", totalMeters / 1000.0, totalSeconds / 60, costoTotal);

        return new DistanciaConTramosResponseDTO(
                origenLat, origenLng,
                destinoLat, destinoLng,
                BigDecimal.valueOf(totalMeters / 1000.0).setScale(3, RoundingMode.HALF_UP),
                totalSeconds / 60,
                legDtos,
                costoTotal.setScale(2, RoundingMode.HALF_UP)
        );
    }

    private String buildWaypointsFromSelectedDepositos(List<Integer> depositoIds) {
        if (depositoIds == null || depositoIds.isEmpty()) {
            log.debug("No hay depósitos seleccionados para waypoints");
            return "";
        }

        depositoIds = List.copyOf(new LinkedHashSet<>(depositoIds));
        List<Deposito> depos = depositoService.findActivosByIdsKeepingOrder(depositoIds);
        if (depos.isEmpty()) {
            log.error("No hay depósitos válidos para los ids {}", depositoIds);
            throw new NoSuchElementException("No hay depósitos válidos para los ids: " + depositoIds);
        }
        if (depos.size() > 23) {
            log.warn("Se permiten hasta 23 depósitos como waypoints, recibidos {}", depos.size());
            throw new IllegalArgumentException("Se permiten hasta 23 depósitos como waypoints.");
        }

        return depos.stream()
                .map(d -> toLatLng(d.getLatitud(), d.getLongitud()))
                .collect(Collectors.joining("|"));
    }

    private String toLatLng(BigDecimal lat, BigDecimal lng) {
        return lat.stripTrailingZeros().toPlainString() + "," + lng.stripTrailingZeros().toPlainString();
    }

    public RutaCalculadaDTO calcularRutaYTramos(Double origenLat, Double origenLng,
                                                Double destinoLat, Double destinoLng,
                                                List<Integer> depositoIds) {
        log.info("Calculando ruta y tramos de {}:{} a {}:{} con depósitos {}", origenLat, origenLng, destinoLat, destinoLng, depositoIds);

        final String origin = origenLat + "," + origenLng;
        final String destination = destinoLat + "," + destinoLng;
        final String waypoints = buildWaypointsFromSelectedDepositos(depositoIds);

        DirectionsResponseDTO response = googleMapsClient.getDirections(origin, destination, "driving", false, waypoints, googleMapsProperties.getApi().getKey());
        if (response == null || !"OK".equals(response.getStatus())) {
            log.error("Error en Google Maps API: {}", (response != null ? response.getStatus() : "null"));
            throw new RuntimeException("Error en Google Maps API: " + (response != null ? response.getStatus() : "null"));
        }

        var route = response.getRoutes().getFirst();
        var legs = route.getLegs();
        if (legs == null || legs.isEmpty()) {
            log.error("Google Maps API devolvió sin rutas/legs");
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

            legDtos.add(new LegCalculadoDTO(BigDecimal.valueOf(meters / 1000.0).setScale(3, RoundingMode.HALF_UP),
                    seconds / 60, depOri, depDes, tipo, null));
            log.debug("Leg {}: tipo {}, depOri {}, depDes {}, distancia {} km", i, tipo, depOri, depDes, meters / 1000.0);
        }

        log.info("Ruta calculada: distancia total {} km, duración total {} min", totalMeters / 1000.0, totalSeconds / 60);

        return new RutaCalculadaDTO(
                BigDecimal.valueOf(totalMeters / 1000.0).setScale(3, RoundingMode.HALF_UP),
                totalSeconds / 60,
                legDtos
        );
    }

    private Tarifa obtenerTarifaVigente() {
        log.debug("Obteniendo tarifa vigente");
        return tarifaRepository.findByActivoTrue().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay tarifa vigente"));
    }

    private BigDecimal obtenerConsumoPromedio() {
        var disponibles = camionService.findAllDisponibles();
        if (disponibles == null || disponibles.isEmpty()) {
            log.warn("No hay camiones disponibles, consumo promedio = 0");
            return BigDecimal.ZERO;
        }
        double avg = disponibles.stream()
                .mapToDouble(c -> c.getConsumoCombustible() == null ? 0.0 : c.getConsumoCombustible())
                .average().orElse(0.0);
        log.debug("Consumo promedio de combustible calculado: {}", avg);
        return BigDecimal.valueOf(avg);
    }

    private BigDecimal calcularCostoAproximadoTramo(BigDecimal distanciaKm, Tarifa tarifa, BigDecimal consumoPromKm) {
        if (distanciaKm == null) return BigDecimal.ZERO;
        BigDecimal costoGestion = BigDecimal.valueOf(tarifa.getValorPorTramo()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal costoKmBase = BigDecimal.valueOf(tarifa.getValorPorKm()).multiply(distanciaKm);
        BigDecimal costoCombustible = BigDecimal.valueOf(tarifa.getValorLitroCombustible())
                .multiply(consumoPromKm).multiply(distanciaKm);
        BigDecimal costoTotal = costoGestion.add(costoKmBase).add(costoCombustible).setScale(2, RoundingMode.HALF_UP);
        log.debug("Costo tramo calculado: distancia {}, costoTotal {}", distanciaKm, costoTotal);
        return costoTotal;
    }
}
