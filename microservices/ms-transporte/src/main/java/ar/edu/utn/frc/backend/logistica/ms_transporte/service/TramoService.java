package ar.edu.utn.frc.backend.logistica.ms_transporte.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.enums.EstadoTramo;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.*;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tramo.*;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import jakarta.transaction.Transactional;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.math.RoundingMode;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TramoService {

    private final TramoRepository tramoRepository;
    private final RutaRepository rutaRepository;
    private final CamionService camionService;
    private final RutaService rutaService;
    private final TarifaRepository tarifaRepository;

    public List<Tramo> findAll() {
        log.info("Obteniendo todos los tramos");
        return tramoRepository.findAll();
    }

    public Tramo findById(int idTramo) {
        log.info("Buscando tramo con ID {}", idTramo);
        return tramoRepository.findById(idTramo)
                .orElseThrow(() -> new NoSuchElementException("Tramo no encontrado"));
    }

    public List<Tramo> findByRuta(int rutaId) {
        log.info("Buscando tramos para ruta {}", rutaId);
        Ruta ruta = rutaRepository.findById(rutaId)
                .orElseThrow(() -> new NoSuchElementException("Ruta no encontrada"));

        return tramoRepository.findByRuta(ruta);
    }

    public TramoCreateResponseDTO crear(TramoCreateRequestDTO dto) {
        log.info("Creando tramo para la ruta {}", dto.getIdRuta());

        final Integer idRuta = Objects.requireNonNull(dto.getIdRuta(), "idRuta no puede ser null");
        Ruta r = rutaRepository.findById(idRuta)
                .orElseThrow(() -> new NoSuchElementException("Ruta no encontrada"));

        Tramo tramo = new Tramo();
        tramo.setRuta(r);
        tramo.setIdDepositoOrigen(dto.getDepositoOrigenId());
        tramo.setIdDepositoDestino(dto.getDepositoDestinoId());
        tramo.setTipo(dto.getTipo());
        tramo.setEstado(EstadoTramo.PLANIFICADO);
        tramo.setDistancia(dto.getDistancia());
        tramo.setCostoAproximado(dto.getCostoAproximado());
        tramo.setFechaHoraInicioEstimada(dto.getFechaHoraInicioEstimada());
        tramo.setFechaHoraFinEstimada(dto.getFechaHoraFinEstimada());

        tramoRepository.save(tramo);
        rutaService.recalcularDesdeTramos(r.getIdRuta());

        log.info("Tramo {} creado", tramo.getIdTramo());

        return new TramoCreateResponseDTO(tramo.getIdTramo(), "Tramo creado correctamente (PLANIFICADO)");
    }

    public TramoResponseDTO actualizar(int idTramo, TramoUpdateRequestDTO dto) {
        log.info("Actualizando tramo {}", idTramo);
        Tramo tramo = findById(idTramo);

        if (tramo.getEstado() == EstadoTramo.INICIADO || tramo.getEstado() == EstadoTramo.FINALIZADO) {
            log.warn("Intento de actualizar tramo en estado {}", tramo.getEstado());
            throw new IllegalStateException("No se puede actualizar un tramo iniciado o finalizado");
        }

        tramo.setIdDepositoOrigen(dto.getDepositoOrigenId());
        tramo.setIdDepositoDestino(dto.getDepositoDestinoId());
        tramo.setDistancia(dto.getDistancia());
        tramo.setDominioCamion(dto.getDominioCamion());
        tramo.setKeyCloakIdTransportista(dto.getKeyCloakIdTransportista());
        tramo.setCostoAproximado(dto.getCostoAproximado());
        tramo.setFechaHoraInicioEstimada(dto.getFechaHoraInicioEstimada());
        tramo.setFechaHoraFinEstimada(dto.getFechaHoraFinEstimada());

        tramoRepository.save(tramo);
        rutaService.recalcularDesdeTramos(tramo.getRuta().getIdRuta());

        log.info("Tramo {} actualizado", idTramo);

        return new TramoResponseDTO(idTramo, "Tramo actualizado correctamente");
    }

    public TramoResponseDTO eliminar(Integer idTramo) {
        log.info("Eliminando tramo {}", idTramo);

        Tramo tramo = findById(idTramo);

        if (tramo.getEstado() == EstadoTramo.INICIADO || tramo.getEstado() == EstadoTramo.FINALIZADO) {
            log.warn("Intento de eliminar tramo en estado {}", tramo.getEstado());
            throw new IllegalStateException("No se puede eliminar un tramo iniciado o finalizado");
        }

        Integer rutaId = tramo.getRuta().getIdRuta();
        tramoRepository.delete(tramo);

        rutaService.recalcularDesdeTramos(rutaId);
        log.info("Tramo {} eliminado", idTramo);

        return new TramoResponseDTO(idTramo, "Tramo eliminado correctamente");
    }

    @Transactional
    public TramoResponseDTO iniciar(int idTramo, TramoLifecycleRequestDTO body, String subject) {
        log.info("Iniciando tramo {} por transportista {}", idTramo, subject);
        Tramo tramo = findById(idTramo);

        if (body == null) body = new TramoLifecycleRequestDTO();

        if (tramo.getEstado() != EstadoTramo.ASIGNADO) {
            log.warn("Intento de iniciar tramo {} en estado {}", idTramo, tramo.getEstado());
            throw new IllegalStateException("Para iniciar, el tramo debe estar en estado ASIGNADO");
        }

        if (!subject.equals(tramo.getKeyCloakIdTransportista())) {
            log.warn("Transportista {} no autorizado para tramo {}", subject, idTramo);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El transportista asignado es el único que puede iniciar el tramo");
        }

        tramo.setEstado(EstadoTramo.INICIADO);
        tramo.setFechaHoraInicio(
                body.getFechaHora() != null ? body.getFechaHora() : LocalDateTime.now()
        );

        tramoRepository.save(tramo);
        rutaService.recalcularDesdeTramos(tramo.getRuta().getIdRuta());

        log.info("Tramo {} iniciado correctamente", idTramo);

        return new TramoResponseDTO(idTramo, "Tramo iniciado");
    }

    // --- FINALIZAR TRAMO ----------------------------------------------------

    @Transactional
    public TramoResponseDTO finalizar(Integer id, TramoLifecycleRequestDTO dto, String subject) {
        log.info("Finalizando tramo {} por transportista {}", id, subject);

        Tramo t = findById(id);

        if (t.getEstado() != EstadoTramo.INICIADO) {
            log.warn("Intento de finalizar tramo {} en estado {}", id, t.getEstado());
            throw new IllegalStateException("El tramo debe estar INICIADO para poder finalizarse");
        }

        if (!subject.equals(t.getKeyCloakIdTransportista())) {
            log.warn("Transportista {} no autorizado para finalizar tramo {}", subject, id);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El transportista asignado es el único que puede finalizar el tramo");
        }

        t.setEstado(EstadoTramo.FINALIZADO);
        t.setFechaHoraFin(dto.getFechaHora() != null ? dto.getFechaHora() : LocalDateTime.now());
        t.setDistancia(dto.getKmRecorridos() != null ? dto.getKmRecorridos() : t.getDistancia());

        BigDecimal km = t.getDistancia() != null ? t.getDistancia() : BigDecimal.ZERO;

        Tarifa tarifa = tarifaRepository.findByActivoTrue().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay tarifa vigente"));

        BigDecimal costoReal = calcularCostoRealTramo(km, tarifa, t.getDominioCamion());
        t.setCostoReal(costoReal);

        tramoRepository.save(t);
        rutaService.recalcularDesdeTramos(t.getRuta().getIdRuta());

        log.info("Tramo {} finalizado. Costo real: {}", id, costoReal);

        return new TramoResponseDTO(t.getIdTramo(),
                "Tramo finalizado. Costo calculado automáticamente: $" + t.getCostoReal());
    }

    private BigDecimal calcularCostoRealTramo(BigDecimal km, Tarifa tarifa, String dominioCamion) {
        log.debug("Calculando costo real para {} km con camion {}", km, dominioCamion);

        if (km == null) km = BigDecimal.ZERO;

        BigDecimal costoGestion = BigDecimal.valueOf(tarifa.getValorPorTramo())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal costoCamionKm = BigDecimal.ZERO;
        BigDecimal costoCombustible = BigDecimal.ZERO;

        if (dominioCamion != null && !dominioCamion.isBlank()) {
            Camion camion = camionService.findByDominio(dominioCamion);
            if (camion != null) {
                BigDecimal costoPorKmCamion = camion.getCostoBase() != null
                        ? camion.getCostoBase()
                        : BigDecimal.ZERO;

                BigDecimal consumo = camion.getConsumoCombustible() != null
                        ? BigDecimal.valueOf(camion.getConsumoCombustible())
                        : BigDecimal.ZERO;

                BigDecimal valorLitro = BigDecimal.valueOf(tarifa.getValorLitroCombustible());

                costoCamionKm = costoPorKmCamion.multiply(km);
                costoCombustible = consumo.multiply(valorLitro).multiply(km);
            }
        }

        return costoGestion.add(costoCamionKm).add(costoCombustible)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // --- ASIGNAR / DESASIGNAR CAMIÓN ----------------------------------------

    @Transactional
    public TramoResponseDTO asignarCamion(Integer id, TramoAsignarCamionRequestDTO dto) {
        log.info("Asignando camión {} al tramo {}", dto.getDominio(), id);

        Tramo tramo = findById(id);

        if (tramo.getEstado() == EstadoTramo.INICIADO ||
                tramo.getEstado() == EstadoTramo.FINALIZADO ||
                tramo.getEstado() == EstadoTramo.CANCELADO) {
            log.warn("No se puede asignar camión al tramo {} en estado {}", id, tramo.getEstado());
            throw new IllegalStateException("No se puede asignar camión en un tramo no editable");
        }

        Camion camion = camionService.findByDominio(dto.getDominio());
        if (camion == null) {
            throw new NoSuchElementException("Camión no encontrado: " + dto.getDominio());
        }

        if (!Boolean.TRUE.equals(camion.getDisponibilidad())) {
            throw new IllegalStateException("El camión no está disponible");
        }

        tramo.setDominioCamion(dto.getDominio());
        if (tramo.getEstado() == EstadoTramo.PLANIFICADO) {
            tramo.setEstado(EstadoTramo.ASIGNADO);
        }

        tramoRepository.save(tramo);
        rutaService.recalcularDesdeTramos(tramo.getRuta().getIdRuta());

        log.info("Camión {} asignado correctamente al tramo {}", dto.getDominio(), id);

        return new TramoResponseDTO(tramo.getIdTramo(), "Camión asignado correctamente");
    }

    @Transactional
    public TramoResponseDTO desasignarCamion(Integer id) {
        log.info("Desasignando camión del tramo {}", id);

        Tramo tramo = findById(id);

        if (tramo.getEstado() == EstadoTramo.INICIADO ||
                tramo.getEstado() == EstadoTramo.FINALIZADO ||
                tramo.getEstado() == EstadoTramo.CANCELADO) {
            log.warn("No se puede desasignar camión del tramo {} en estado {}", id, tramo.getEstado());
            throw new IllegalStateException("No se puede desasignar camión en un tramo no editable");
        }

        tramo.setDominioCamion(null);
        if (tramo.getEstado() == EstadoTramo.ASIGNADO) {
            tramo.setEstado(EstadoTramo.PLANIFICADO);
        }

        tramoRepository.save(tramo);
        rutaService.recalcularDesdeTramos(tramo.getRuta().getIdRuta());

        log.info("Camión desasignado del tramo {}", id);

        return new TramoResponseDTO(tramo.getIdTramo(), "Camión desasignado correctamente");
    }
}
