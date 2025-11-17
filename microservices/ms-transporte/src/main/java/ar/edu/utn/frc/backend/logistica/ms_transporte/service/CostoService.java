package ar.edu.utn.frc.backend.logistica.ms_transporte.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Tarifa;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.TarifaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class CostoService {

    private final TarifaRepository tarifaRepository;
    private final CamionService camionService;

    public BigDecimal calcularCosto(BigDecimal distanciaKm) {
        if (distanciaKm == null) return BigDecimal.ZERO;
        Tarifa tarifa = obtenerTarifaVigente();
        BigDecimal consumoPromedio = obtenerConsumoPromedio();
        return calcularCostoAproximadoTramo(distanciaKm, tarifa, consumoPromedio);
    }

    private Tarifa obtenerTarifaVigente() {
        return tarifaRepository.findByActivoTrue().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay tarifa vigente"));
    }

    private BigDecimal obtenerConsumoPromedio() {
        var disponibles = camionService.findAllDisponibles();
        if (disponibles == null || disponibles.isEmpty()) return BigDecimal.ZERO;
        double avg = disponibles.stream()
                .mapToDouble(c -> c.getConsumoCombustible() == null ? 0.0 : c.getConsumoCombustible())
                .average().orElse(0.0);
        return BigDecimal.valueOf(avg);
    }

    private BigDecimal calcularCostoAproximadoTramo(BigDecimal distanciaKm, Tarifa tarifa, BigDecimal consumoPromKm) {
        BigDecimal costoGestion = BigDecimal.valueOf(tarifa.getValorPorTramo()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal costoKmBase = BigDecimal.valueOf(tarifa.getValorPorKm()).multiply(distanciaKm);
        BigDecimal costoCombustible = BigDecimal.valueOf(tarifa.getValorLitroCombustible())
                .multiply(consumoPromKm).multiply(distanciaKm);
        return costoGestion.add(costoKmBase).add(costoCombustible).setScale(2, RoundingMode.HALF_UP);
    }
}

