package ar.edu.utn.frc.backend.logistica.ms_transporte.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tramo.TramoAsignarCamionRequestDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tramo.TramoCreateRequestDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tramo.TramoCreateResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tramo.TramoLifecycleRequestDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tramo.TramoResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tramo.TramoUpdateRequestDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Camion;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Ruta;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Tramo;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.enums.EstadoTramo;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.RutaRepository;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.TramoRepository;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Tarifa;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.TarifaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TramoService {

    private final TramoRepository tramoRepository;
    private final RutaRepository rutaRepository;
    private final CamionService camionService;
    private final RutaService rutaService;
    private final TarifaRepository tarifaRepository;

    public List<Tramo> findAll() {
        return tramoRepository.findAll();
    }

    public Tramo findById(int idTramo) {
        return tramoRepository.findById(idTramo)
                .orElseThrow(() -> new NoSuchElementException("Tramo no encontrado"));
    }

    public List<Tramo> findByRuta(int rutaId) {
        Ruta ruta = rutaRepository.findById(rutaId)
                .orElseThrow(() -> new NoSuchElementException("Ruta no encontrada"));
        return tramoRepository.findByRuta(ruta);
    }

    public TramoCreateResponseDTO crear(TramoCreateRequestDTO dto) {
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

        return new TramoCreateResponseDTO(tramo.getIdTramo(), "Tramo creado correctamente (PLANIFICADO)");
    }

    public TramoResponseDTO actualizar(int idTramo, TramoUpdateRequestDTO dto) {
        Tramo tramo = findById(idTramo);

        if (tramo.getEstado() == EstadoTramo.INICIADO || tramo.getEstado() == EstadoTramo.FINALIZADO) {
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

        return new TramoResponseDTO(idTramo, "Tramo actualizado correctamente");
    }

    public TramoResponseDTO eliminar(Integer idTramo) {
        Tramo tramo = findById(idTramo);

        if (tramo.getEstado() == EstadoTramo.INICIADO || tramo.getEstado() == EstadoTramo.FINALIZADO) {
            throw new IllegalStateException("No se puede eliminar un tramo iniciado o finalizado");
        }

        Integer rutaId = tramo.getRuta().getIdRuta();
        tramoRepository.delete(tramo);

        rutaService.recalcularDesdeTramos(rutaId);
        return new TramoResponseDTO(idTramo, "Tramo eliminado correctamente");
    }

    @Transactional
    public TramoResponseDTO iniciar(int idTramo, TramoLifecycleRequestDTO body, String subject) {
        Tramo tramo = findById(idTramo);

        if (body == null) body = new TramoLifecycleRequestDTO();

        if (tramo.getEstado() == EstadoTramo.INICIADO || tramo.getEstado() == EstadoTramo.FINALIZADO) {
            throw new IllegalStateException("El tramo ya fue iniciado o finalizado");
        }
        if (tramo.getEstado() == EstadoTramo.CANCELADO) {
            throw new IllegalStateException("El tramo está cancelado");
        }

        if (tramo.getEstado() != EstadoTramo.ASIGNADO) {
            throw new IllegalStateException("Para iniciar, el tramo debe estar en estado ASIGNADO");
        }

        if (tramo.getDominioCamion() == null || tramo.getDominioCamion().isBlank()) {
            throw new IllegalStateException("No se puede iniciar un tramo sin camión asignado");
        }

        if (tramo.getKeyCloakIdTransportista() == null || tramo.getKeyCloakIdTransportista().isBlank()) {
            throw new IllegalStateException("No se puede iniciar un tramo sin transportista asignado");
        }

        if (!tramo.getKeyCloakIdTransportista().equals(subject)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El transportista asignado es el único que puede iniciar el tramo");
        }

        tramo.setEstado(EstadoTramo.INICIADO);
        tramo.setFechaHoraInicio(
            body.getFechaHora() != null ? body.getFechaHora() : LocalDateTime.now()
        );

        tramoRepository.save(tramo);
        rutaService.recalcularDesdeTramos(tramo.getRuta().getIdRuta());

        return new TramoResponseDTO(idTramo, "Tramo iniciado");
    }

    @Transactional
    public TramoResponseDTO finalizar(Integer id, TramoLifecycleRequestDTO dto, String subject) {
        Tramo t = findById(id);

        if (t.getEstado() != EstadoTramo.INICIADO) {
            throw new IllegalStateException("El tramo debe estar INICIADO para poder finalizarse");
        }

        if (t.getFechaHoraInicio() != null &&
                dto.getFechaHora() != null &&
                dto.getFechaHora().isBefore(t.getFechaHoraInicio())) {
            throw new IllegalStateException("La fecha de fin no puede ser anterior al inicio");
        }

        if (t.getKeyCloakIdTransportista() == null || t.getKeyCloakIdTransportista().isBlank()) {
            throw new IllegalStateException("No se puede finalizar un tramo sin transportista asignado");
        }
        if (!t.getKeyCloakIdTransportista().equals(subject)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El transportista asignado es el único que puede finalizar el tramo");
        }

        t.setEstado(EstadoTramo.FINALIZADO);
        t.setFechaHoraFin(dto.getFechaHora() != null ? dto.getFechaHora() : LocalDateTime.now());
        t.setDistancia(dto.getKmRecorridos());

        BigDecimal km = dto.getKmRecorridos();
        if (km == null) {
            km = t.getDistancia();
        }
        if (km == null) {
            km = BigDecimal.ZERO;
        }

        Tarifa tarifa = tarifaRepository.findByActivoTrue().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay tarifa vigente"));

        BigDecimal costoReal = calcularCostoRealTramo(km, tarifa, t.getDominioCamion());
        t.setCostoReal(costoReal);

        tramoRepository.save(t);
        rutaService.recalcularDesdeTramos(t.getRuta().getIdRuta());

        return new TramoResponseDTO(t.getIdTramo(),
                "Tramo finalizado. Costo calculado automáticamente: $" + t.getCostoReal());
    }

    private BigDecimal calcularCostoRealTramo(BigDecimal km, Tarifa tarifa, String dominioCamion) {
        if (km == null) km = BigDecimal.ZERO;
        BigDecimal costoGestion = BigDecimal.valueOf(tarifa.getValorPorTramo()).setScale(2, RoundingMode.HALF_UP);

        BigDecimal costoCamionKm = BigDecimal.ZERO;
        BigDecimal costoCombustible = BigDecimal.ZERO;

        if (dominioCamion != null && !dominioCamion.isBlank()) {
            Camion camion = camionService.findByDominio(dominioCamion);
            if (camion != null) {
                BigDecimal costoPorKmCamion = camion.getCostoBase() == null ? BigDecimal.ZERO : camion.getCostoBase();
                BigDecimal consumo = camion.getConsumoCombustible() == null
                        ? BigDecimal.ZERO
                        : BigDecimal.valueOf(camion.getConsumoCombustible());
                BigDecimal valorLitro = BigDecimal.valueOf(tarifa.getValorLitroCombustible());

                costoCamionKm = costoPorKmCamion.multiply(km);
                costoCombustible = consumo.multiply(valorLitro).multiply(km);
            }
        }

        return costoGestion.add(costoCamionKm).add(costoCombustible).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public TramoResponseDTO asignarCamion(Integer id, TramoAsignarCamionRequestDTO dto) {
        Tramo tramo = findById(id);

        if (tramo.getEstado() == EstadoTramo.INICIADO
                || tramo.getEstado() == EstadoTramo.FINALIZADO
                || tramo.getEstado() == EstadoTramo.CANCELADO) {
            throw new IllegalStateException("No se puede asignar camión en un tramo no editable");
        }

        if (dto == null || dto.getDominio() == null || dto.getDominio().isBlank()) {
            throw new IllegalArgumentException("El dominio es obligatorio");
        }

        Camion camion = camionService.findByDominio(dto.getDominio());
        if (camion == null) {
            throw new NoSuchElementException("Camión no encontrado: " + dto.getDominio());
        }

        if (!Boolean.TRUE.equals(camion.getDisponibilidad())) {
            throw new IllegalStateException("El camión no está disponible");
        }

        String actual = tramo.getDominioCamion() == null ? "" : tramo.getDominioCamion();
        if (dto.getDominio().equalsIgnoreCase(actual)) {
            return new TramoResponseDTO(tramo.getIdTramo(), "El camión ya estaba asignado");
        }

        tramo.setDominioCamion(dto.getDominio());
        if (tramo.getEstado() == EstadoTramo.PLANIFICADO) {
            tramo.setEstado(EstadoTramo.ASIGNADO);
        }

        tramoRepository.save(tramo);
        rutaService.recalcularDesdeTramos(tramo.getRuta().getIdRuta());

        return new TramoResponseDTO(tramo.getIdTramo(), "Camión asignado correctamente");
    }

    @Transactional
    public TramoResponseDTO desasignarCamion(Integer id) {
        Tramo tramo = findById(id);

        if (tramo.getEstado() == EstadoTramo.INICIADO
                || tramo.getEstado() == EstadoTramo.FINALIZADO
                || tramo.getEstado() == EstadoTramo.CANCELADO) {
            throw new IllegalStateException("No se puede desasignar camión en un tramo no editable");
        }

        if (tramo.getDominioCamion() == null || tramo.getDominioCamion().isBlank()) {
            return new TramoResponseDTO(tramo.getIdTramo(), "El tramo ya no tenía camión asignado");
        }

        tramo.setDominioCamion(null);
        if (tramo.getEstado() == EstadoTramo.ASIGNADO) {
            tramo.setEstado(EstadoTramo.PLANIFICADO);
        }

        tramoRepository.save(tramo);
        rutaService.recalcularDesdeTramos(tramo.getRuta().getIdRuta());

        return new TramoResponseDTO(tramo.getIdTramo(), "Camión desasignado correctamente");
    }



}
