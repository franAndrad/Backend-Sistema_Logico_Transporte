package ar.edu.utn.frc.backend.logistica.ms_transporte.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.TarifaRepository;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Tarifa;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.math.RoundingMode;
import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class CostoService {

    private final TarifaRepository tarifaRepository;
    private final CamionService camionService;

    public BigDecimal calcularCosto(BigDecimal distanciaKm) {
        log.info("Calculando costo para distancia {} km", distanciaKm);

        if (distanciaKm == null) {
            log.warn("La distancia es null, se retorna 0");
            return BigDecimal.ZERO;
        }

        Tarifa tarifa = obtenerTarifaVigente();
        BigDecimal consumoPromedio = obtenerConsumoPromedio();

        BigDecimal costo = calcularCostoAproximadoTramo(distanciaKm, tarifa, consumoPromedio);
        log.info("Costo calculado: {}", costo);

        return costo;
    }

    private Tarifa obtenerTarifaVigente() {
        log.debug("Obteniendo tarifa vigente");
        return tarifaRepository.findByActivoTrue().stream().findFirst()
                .orElseThrow(() -> {
                    log.error("No hay tarifa vigente");
                    return new IllegalStateException("No hay tarifa vigente");
                });
    }

    private BigDecimal obtenerConsumoPromedio() {
        log.debug("Calculando consumo promedio de camiones disponibles");
        var disponibles = camionService.findAllDisponibles();

        if (disponibles == null || disponibles.isEmpty()) {
            log.warn("No hay camiones disponibles, consumo promedio = 0");
            return BigDecimal.ZERO;
        }

        double avg = disponibles.stream()
                .mapToDouble(c -> c.getConsumoCombustible() == null ? 0.0 : c.getConsumoCombustible())
                .average().orElse(0.0);

        BigDecimal promedio = BigDecimal.valueOf(avg);
        log.debug("Consumo promedio calculado: {}", promedio);
        return promedio;
    }

    private BigDecimal calcularCostoAproximadoTramo(BigDecimal distanciaKm, Tarifa tarifa, BigDecimal consumoPromKm) {
        log.debug("Calculando costo aproximado del tramo: {} km, tarifa {}, consumoPromKm {}", distanciaKm, tarifa.getIdTarifa(), consumoPromKm);

        BigDecimal costoGestion = BigDecimal.valueOf(tarifa.getValorPorTramo()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal costoKmBase = BigDecimal.valueOf(tarifa.getValorPorKm()).multiply(distanciaKm);
        BigDecimal costoCombustible = BigDecimal.valueOf(tarifa.getValorLitroCombustible())
                .multiply(consumoPromKm).multiply(distanciaKm);

        BigDecimal total = costoGestion.add(costoKmBase).add(costoCombustible).setScale(2, RoundingMode.HALF_UP);
        log.debug("Costo gestión: {}, costo km base: {}, costo combustible: {}, total: {}", costoGestion, costoKmBase, costoCombustible, total);

        return total;
    }
}
