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
import ar.edu.utn.frc.backend.logistica.ms_transporte.client.ClienteClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class RutaService {

    private final RutaRepository rutaRepository;
    private final TramoRepository tramoRepository;
    private final GoogleMapsService googleMapsService;
    private final CostoService costoService;
    private final ClienteClient clienteClient;

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

        LocalDateTime cursor = LocalDateTime.now();

        for (LegCalculadoDTO leg : calculada.getLegs()) {
            Tramo tramo = new Tramo();
            tramo.setRuta(ruta);
            tramo.setIdDepositoOrigen(leg.getDepositoOrigenId());
            tramo.setIdDepositoDestino(leg.getDepositoDestinoId());
            tramo.setTipo(TipoTramo.valueOf(leg.getTipo())); 
            tramo.setEstado(EstadoTramo.PLANIFICADO);
            tramo.setDistancia(leg.getDistanciaKm());
            tramo.setCostoAproximado(costoService.calcularCosto(leg.getDistanciaKm()));
            tramo.setFechaHoraInicioEstimada(cursor);
            long durMin = leg.getDuracionMin() == null ? 0L : leg.getDuracionMin();
            cursor = cursor.plusMinutes(durMin);
            tramo.setFechaHoraFinEstimada(cursor);
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

    private void actualizarEstadoSolicitud(Integer idSolicitud, String estado, String descripcion) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("estado", estado);
            body.put("descripcion", descripcion);
            log.info("[RutaService] Actualizando estado de solicitud {} -> {} ({})", idSolicitud, estado, descripcion);
            clienteClient.actualizarEstado(idSolicitud, body);
            log.info("[RutaService] Estado de solicitud {} actualizado a {}", idSolicitud, estado);
        } catch (Exception ex) {
            log.warn("[RutaService] Falló actualizar estado de solicitud {} -> {}: {}", idSolicitud, estado, ex.toString());
        }
    }

    public void recalcularDesdeTramos(Integer idRuta) {
        Ruta ruta = findById(idRuta);
        EstadoRuta estadoAnterior = ruta.getEstado();
        List<Tramo> tramos = tramoRepository.findByRuta(ruta);

        ruta.setCantidadTramos(tramos.size());

        BigDecimal totalKm = tramos.stream()
                .filter(t -> t.getEstado() != EstadoTramo.CANCELADO)
                .map(t -> t.getDistancia() == null ? BigDecimal.ZERO : t.getDistancia())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ruta.setDistanciaTotal(totalKm);

        long finalizados = tramos.stream().filter(t -> t.getEstado() == EstadoTramo.FINALIZADO).count();
        long iniciados = tramos.stream().filter(t -> t.getEstado() == EstadoTramo.INICIADO).count();
        long asignados = tramos.stream().filter(t -> t.getEstado() == EstadoTramo.ASIGNADO).count();
        long planifOAsign = tramos.stream()
                .filter(t -> t.getEstado() == EstadoTramo.PLANIFICADO || t.getEstado() == EstadoTramo.ASIGNADO)
                .count();
        boolean allCancelados = !tramos.isEmpty() && tramos.stream().allMatch(t -> t.getEstado() == EstadoTramo.CANCELADO);
        boolean anyDepositoIniciado = tramos.stream().anyMatch(t ->
                t.getEstado() == EstadoTramo.INICIADO && t.getTipo() == TipoTramo.DEPOSITO_DEPOSITO
        );

        EstadoRuta nuevoEstado;
        if (tramos.isEmpty()) {
            nuevoEstado = EstadoRuta.ESTIMADA;
        } else if (finalizados == tramos.size()) {
            nuevoEstado = EstadoRuta.COMPLETADA;
        } else if (iniciados > 0) {
            nuevoEstado = EstadoRuta.EN_PROGRESO;
        } else if (planifOAsign > 0) {
            nuevoEstado = EstadoRuta.ASIGNADA;
        } else {
            nuevoEstado = EstadoRuta.ESTIMADA;
        }

        ruta.setEstado(nuevoEstado);
        rutaRepository.save(ruta);

        Integer idSolicitud = ruta.getIdSolicitud();
        if (idSolicitud != null) {
            if (estadoAnterior != EstadoRuta.COMPLETADA && nuevoEstado == EstadoRuta.COMPLETADA) {
                actualizarEstadoSolicitud(idSolicitud, "ENTREGADA", "Ruta completada");
                return;
            }
            if (allCancelados) {
                actualizarEstadoSolicitud(idSolicitud, "CANCELADA", "Ruta cancelada");
                return;
            }
            if (anyDepositoIniciado) {
                actualizarEstadoSolicitud(idSolicitud, "EN_DEPOSITO", "En depósito intermedio");
                return;
            }
            if (iniciados > 0) {
                actualizarEstadoSolicitud(idSolicitud, "EN_TRANSITO", "Transporte en curso");
                return;
            }
            if (asignados > 0) {
                actualizarEstadoSolicitud(idSolicitud, "ASIGNADA", "Ruta asignada");
                return;
            }
        }
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
}
