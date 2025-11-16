package ar.edu.utn.frc.backend.logistica.ms_transporte.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.ruta.LegCalculadoDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.ruta.RutaCalculadaDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.ruta.RutaCreateRequestDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.ruta.RutaCreateResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.ruta.RutaResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.ruta.RutaUpdateRequestDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Ruta;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Tramo;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.enums.EstadoRuta;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.enums.EstadoTramo;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.enums.TipoTramo;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.RutaRepository;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.TramoRepository;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Tarifa;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.TarifaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RutaService {

    private final RutaRepository rutaRepository;
    private final TramoRepository tramoRepository;
    private final GoogleMapsService googleMapsService;
    private final TarifaRepository tarifaRepository;
    private final CamionService camionService;

    public List<Ruta> findAll() {
        return rutaRepository.findAll();
    }

    public Ruta findById(Integer idRuta) {
        if (idRuta == null) {
            throw new IllegalArgumentException("El ID de la ruta no puede ser nulo");
        }
        return rutaRepository.findById(idRuta)
                .orElseThrow(() -> new NoSuchElementException("Ruta no encontrada"));
    }

    public Ruta findBySolicitud(Integer idSolicitud) {
        return rutaRepository.findByIdSolicitud(idSolicitud)
                .orElseThrow(() -> new NoSuchElementException("No existe ruta para la solicitud: " + idSolicitud));
    }

    @Transactional
    public RutaCreateResponseDTO crear(RutaCreateRequestDTO dto) {
        RutaCalculadaDTO calculada = googleMapsService.calcularRutaYTramos(
                dto.getOrigenLat().doubleValue(),
                dto.getOrigenLon().doubleValue(),
                dto.getDestinoLat().doubleValue(),
                dto.getDestinoLon().doubleValue(),
                dto.getDepositoIds());

        Ruta ruta = new Ruta();
        ruta.setIdSolicitud(dto.getIdSolicitud());
        ruta.setEstado(EstadoRuta.ESTIMADA);
        ruta.setCantidadDepositos(dto.getDepositoIds() == null ? 0 : dto.getDepositoIds().size());
        ruta.setCantidadTramos(calculada.getLegs().size());
        ruta.setDistanciaTotal(calculada.getDistanciaTotalKm());
        rutaRepository.save(ruta);

        Tarifa tarifa = obtenerTarifaVigente();
        BigDecimal consumoPromedio = obtenerConsumoPromedio();

        for (LegCalculadoDTO leg : calculada.getLegs()) {
            Tramo tramo = new Tramo();
            tramo.setRuta(ruta);
            tramo.setIdDepositoOrigen(leg.getDepositoOrigenId());
            tramo.setIdDepositoDestino(leg.getDepositoDestinoId());
            tramo.setTipo(TipoTramo.valueOf(leg.getTipo())); 
            tramo.setEstado(EstadoTramo.PLANIFICADO);
            tramo.setDistancia(leg.getDistanciaKm());
            tramo.setCostoAproximado(calcularCostoAproximadoTramo(leg.getDistanciaKm(), tarifa, consumoPromedio));
            tramoRepository.save(tramo);
        }

        return new RutaCreateResponseDTO(
                ruta.getIdRuta(),
                "Ruta creada con " + ruta.getCantidadTramos() + " tramos planificados");
    }

    public RutaResponseDTO actualizar(Integer idRuta, RutaUpdateRequestDTO dto) {
        Ruta ruta = findById(idRuta);

        if (ruta.getEstado() == EstadoRuta.COMPLETADA) {
            throw new IllegalStateException("No se puede actualizar una ruta completada");
        }

        ruta.setIdSolicitud(dto.getIdSolicitud());
        rutaRepository.save(ruta);

        return new RutaResponseDTO(ruta.getIdRuta(), "Ruta actualizada correctamente");
    }

    public void recalcularDesdeTramos(Integer idRuta) {
        Ruta ruta = findById(idRuta);
        List<Tramo> tramos = tramoRepository.findByRuta(ruta);

        ruta.setCantidadTramos(tramos.size());

        BigDecimal totalKm = tramos.stream()
                .filter(t -> t.getEstado() != EstadoTramo.CANCELADO)
                .map(t -> t.getDistancia() == null ? BigDecimal.ZERO : t.getDistancia())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ruta.setDistanciaTotal(totalKm);

        long finalizados = tramos.stream().filter(t -> t.getEstado() == EstadoTramo.FINALIZADO).count();
        long iniciados = tramos.stream().filter(t -> t.getEstado() == EstadoTramo.INICIADO).count();
        long planifOAsign = tramos.stream()
                .filter(t -> t.getEstado() == EstadoTramo.PLANIFICADO || t.getEstado() == EstadoTramo.ASIGNADO)
                .count();

        if (tramos.isEmpty()) {
            ruta.setEstado(EstadoRuta.ESTIMADA);
        } else if (finalizados == tramos.size()) {
            ruta.setEstado(EstadoRuta.COMPLETADA);
        } else if (iniciados > 0) {
            ruta.setEstado(EstadoRuta.EN_PROGRESO);
        } else if (planifOAsign > 0) {
            ruta.setEstado(EstadoRuta.ASIGNADA);
        } else {
            ruta.setEstado(EstadoRuta.ESTIMADA);
        }

        rutaRepository.save(ruta);
    }

    @Transactional
    public RutaResponseDTO eliminar(Integer idRuta) {
        if (idRuta == null) {
            throw new IllegalArgumentException("El ID de la ruta no puede ser nulo");
        }

        Ruta ruta = findById(idRuta);
        if (ruta == null) {
            throw new NoSuchElementException("Ruta no encontrada");
        }

        List<Tramo> tramos = tramoRepository.findByRuta(ruta);
        boolean tieneIniciadosOFInalizados = tramos.stream()
                .anyMatch(t -> t.getEstado().isIniciadoOFinalizado());

        if (tieneIniciadosOFInalizados) {
            throw new IllegalStateException("No se puede eliminar una ruta con tramos iniciados o finalizados");
        }

        if (!tramos.isEmpty()) {
            tramoRepository.deleteAll(tramos);
        }

        rutaRepository.delete(ruta);
        return new RutaResponseDTO(idRuta, "Ruta eliminada correctamente");
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
        if (distanciaKm == null) return null;
        BigDecimal costoGestion = BigDecimal.valueOf(tarifa.getValorPorTramo()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal costoKmBase = BigDecimal.valueOf(tarifa.getValorPorKm()).multiply(distanciaKm);
        BigDecimal costoCombustible = BigDecimal.valueOf(tarifa.getValorLitroCombustible())
                .multiply(consumoPromKm).multiply(distanciaKm);
        return costoGestion.add(costoKmBase).add(costoCombustible).setScale(2, RoundingMode.HALF_UP);
    }
}
