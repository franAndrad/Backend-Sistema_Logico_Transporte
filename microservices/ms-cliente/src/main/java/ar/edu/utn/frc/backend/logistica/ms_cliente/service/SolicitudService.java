package ar.edu.utn.frc.backend.logistica.ms_cliente.service;

import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.contenedor.ContenedorSummaryDTO;
import ar.edu.utn.frc.backend.logistica.ms_cliente.client.transporte.TransporteClient;
import ar.edu.utn.frc.backend.logistica.ms_cliente.repository.ContenedorRepository;
import ar.edu.utn.frc.backend.logistica.ms_cliente.repository.SolicitudRepository;
import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.cliente.ClienteDetailsDTO;
import ar.edu.utn.frc.backend.logistica.ms_cliente.repository.ClienteRepository;
import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.SolicitudEstado;
import ar.edu.utn.frc.backend.logistica.ms_cliente.client.transporte.dto.*;
import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.Contenedor;
import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.Solicitud;
import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.Cliente;
import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.solicitud.*;
import org.springframework.stereotype.Service;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Objects;
import java.util.List;

@Service
@Slf4j
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final ClienteRepository clienteRepository;
    private final ContenedorRepository contenedorRepository;
    private final TransporteClient transporteClient;

    public SolicitudService(SolicitudRepository solicitudRepository,
                            ClienteRepository clienteRepository,
                            ContenedorRepository contenedorRepository,
                            TransporteClient transporteClient) {
        this.solicitudRepository = solicitudRepository;
        this.clienteRepository = clienteRepository;
        this.contenedorRepository = contenedorRepository;
        this.transporteClient = transporteClient;
    }

    public List<SolicitudListDTO> listAll() {
        log.info("Listando todas las solicitudes");
        List<SolicitudListDTO> result = solicitudRepository.findAll().stream()
                .map(s -> new SolicitudListDTO(
                        s.getIdSolicitud(),
                        s.getCliente().getIdCliente(),
                        s.getEstado(),
                        s.getCostoEstimado(),
                        s.getFechaCreacion()
                ))
                .collect(Collectors.toList());
        log.info("Se encontraron {} solicitudes", result.size());
        return result;
    }

    private Integer calcularTiempoEstimadoMin(List<TramoDto> tramos) {
        if (tramos == null || tramos.isEmpty()) return null;
        long total = 0L;
        boolean any = false;
        for (TramoDto t : tramos) {
            if (t.getFechaHoraInicioEstimada() != null && t.getFechaHoraFinEstimada() != null) {
                long mins = Duration.between(t.getFechaHoraInicioEstimada(), t.getFechaHoraFinEstimada()).toMinutes();
                if (mins > 0) {
                    total += mins;
                    any = true;
                }
            }
        }
        return any ? (int) total : null;
    }

    private Integer calcularTiempoRealMin(List<TramoDto> tramos) {
        if (tramos == null || tramos.isEmpty()) return null;
        long totalSeconds = 0L;
        boolean anyPositive = false;
        for (TramoDto t : tramos) {
            if (t.getFechaHoraInicio() != null && t.getFechaHoraFin() != null) {
                long secs = Duration.between(t.getFechaHoraInicio(), t.getFechaHoraFin()).getSeconds();
                if (secs > 0) {
                    totalSeconds += secs;
                    anyPositive = true;
                }
            }
        }
        if (!anyPositive) return null;
        int minutes = (int) Math.ceil(totalSeconds / 60.0);
        return Math.max(minutes, 1);
    }

    public SolicitudDetailsDTO getById(int id) {
        log.info("Obteniendo solicitud con id {}", id);
        Solicitud s = solicitudRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Solicitud no encontrada con id {}", id);
                    return new NoSuchElementException("Solicitud no encontrada");
                });

        if (s.getTiempoEstimado() == null) {
            try {
                log.debug("Backfilleando tiempoEstimado desde tramos para solicitud {}", id);
                RutaDto rutaDtoTmp = transporteClient.obtenerRutaPorSolicitud(s.getIdSolicitud());
                if (rutaDtoTmp != null && rutaDtoTmp.getIdRuta() != null) {
                    List<TramoDto> tramosTmp = transporteClient.obtenerTramosPorRuta(rutaDtoTmp.getIdRuta());
                    Integer mins = calcularTiempoEstimadoMin(tramosTmp);
                    if (mins != null) {
                        s.setTiempoEstimado(mins);
                        solicitudRepository.save(s);
                        log.debug("TiempoEstimado backfilled: {} mins", mins);
                    }
                }
            } catch (RuntimeException ex) {
                log.warn("No fue posible backfillear tiempoEstimado: {}", ex.getMessage());
            }
        }

        RutaDto rutaDto = null;
        List<TramoDto> tramosDto = null;
        try {
            rutaDto = transporteClient.obtenerRutaPorSolicitud(s.getIdSolicitud());
            if (rutaDto != null && rutaDto.getIdRuta() != null) {
                tramosDto = transporteClient.obtenerTramosPorRuta(rutaDto.getIdRuta());
                if (s.getEstado() == SolicitudEstado.ENTREGADA) {
                    if (s.getCostoFinal() == null) {
                        BigDecimal totalReal = tramosDto.stream()
                                .map(t -> t.getCostoReal() == null ? BigDecimal.ZERO : t.getCostoReal())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        s.setCostoFinal(totalReal.doubleValue());
                        log.debug("CostoFinal calculado: {}", totalReal);
                    }
                    if (s.getTiempoReal() == null) {
                        Integer minsReal = calcularTiempoRealMin(tramosDto);
                        if (minsReal != null) s.setTiempoReal(minsReal);
                        log.debug("TiempoReal calculado: {} mins", s.getTiempoReal());
                    }
                    if (s.getCostoFinal() != null || s.getTiempoReal() != null) {
                        try { solicitudRepository.save(s); } catch (RuntimeException ignore) {}
                    }
                }
            }
        } catch (RuntimeException ex) {
            log.warn("No se pudieron obtener datos de ruta/tramos para la solicitud {}: {}", s.getIdSolicitud(), ex.getMessage());
        }

        Cliente c = s.getCliente();
        Contenedor cont = s.getContenedor();
        ClienteDetailsDTO clienteDTO = new ClienteDetailsDTO(
                c.getIdCliente(), c.getKeycloakId(), c.getDireccionFacturacion(),
                c.getDireccionEnvio(), c.getRazonSocial(), c.getCuit());
        ContenedorSummaryDTO contResumen = new ContenedorSummaryDTO(cont.getIdContenedor(), cont.getIdentificacion());

        return new SolicitudDetailsDTO(
                s.getIdSolicitud(),
                s.getOrigenDireccion(),
                s.getDestinoDireccion(),
                s.getEstado(),
                s.getCostoEstimado(),
                s.getTiempoEstimado(),
                cont.getIdentificacion(),
                clienteDTO,
                contResumen,
                rutaDto,
                tramosDto,
                s.getCostoFinal(),
                s.getTiempoReal()
        );
    }

    public List<SolicitudListDTO> listByCliente(int clienteId) {
        log.info("Listando solicitudes para cliente id {}", clienteId);
        List<SolicitudListDTO> result = solicitudRepository.findByCliente_IdCliente(clienteId).stream()
                .map(s -> new SolicitudListDTO(
                        s.getIdSolicitud(),
                        s.getCliente().getIdCliente(),
                        s.getEstado(),
                        s.getCostoEstimado(),
                        s.getFechaCreacion()
                ))
                .collect(Collectors.toList());
        log.info("Se encontraron {} solicitudes para cliente id {}", result.size(), clienteId);
        return result;
    }

    public SolicitudEstadoDTO getEstado(int id) {
        log.info("Obteniendo estado de la solicitud {}", id);
        Solicitud s = solicitudRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Solicitud no encontrada con id {}", id);
                    return new NoSuchElementException("Solicitud no encontrada");
                });
        return new SolicitudEstadoDTO(s.getIdSolicitud(), s.getEstado(), s.getFechaActualizacion(), s.getDescripcionEstado());
    }

    public SolicitudResponseDTO create(SolicitudCreateDTO dto) {
        final Integer idCliente = Objects.requireNonNull(dto.getIdCliente(), "idCliente no puede ser null");
        final Integer idContenedor = Objects.requireNonNull(dto.getIdContenedor(), "idContenedor no puede ser null");
        log.info("Creando solicitud para cliente id {}, contenedor id {}", idCliente, idContenedor);

        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> {
                    log.error("Cliente no encontrado con id {}", idCliente);
                    return new NoSuchElementException("Cliente no encontrado");
                });
        Contenedor contenedor = contenedorRepository.findById(idContenedor)
                .orElseThrow(() -> {
                    log.error("Contenedor no encontrado con id {}", idContenedor);
                    return new NoSuchElementException("Contenedor no encontrado");
                });

        if (!contenedor.getCliente().getIdCliente().equals(cliente.getIdCliente())) {
            log.error("El contenedor {} no pertenece al cliente {}", idContenedor, idCliente);
            throw new IllegalStateException("El contenedor no pertenece al cliente");
        }

        Solicitud s = new Solicitud();
        s.setCliente(cliente);
        s.setContenedor(contenedor);
        s.setOrigenDireccion(dto.getOrigenDireccion());
        s.setOrigenLatitud(dto.getOrigenLatitud());
        s.setOrigenLongitud(dto.getOrigenLongitud());
        s.setDestinoDireccion(dto.getDestinoDireccion());
        s.setDestinoLatitud(dto.getDestinoLatitud());
        s.setDestinoLongitud(dto.getDestinoLongitud());

        Solicitud saved = solicitudRepository.save(s);
        log.info("Solicitud creada con id {}", saved.getIdSolicitud());
        return new SolicitudResponseDTO(saved.getIdSolicitud(), "Solicitud creada (BORRADOR)");
    }

    public SolicitudResponseDTO update(int id, SolicitudUpdateDTO dto) {
        log.info("Actualizando solicitud con id {}", id);
        Solicitud s = solicitudRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Solicitud no encontrada con id {}", id);
                    return new NoSuchElementException("Solicitud no encontrada");
                });
        s.setEstado(dto.getEstado());
        s.setTarifaId(dto.getTarifaId());
        Solicitud saved = solicitudRepository.save(s);
        log.info("Solicitud {} actualizada", saved.getIdSolicitud());
        return new SolicitudResponseDTO(saved.getIdSolicitud(), "Solicitud actualizada");
    }

    public SolicitudResponseDTO updateEstado(int id, SolicitudEstadoUpdateDTO dto) {
        log.info("Actualizando estado de solicitud {} a {}", id, dto.getEstado());
        Solicitud s = solicitudRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Solicitud no encontrada con id {}", id);
                    return new NoSuchElementException("Solicitud no encontrada");
                });
        s.setEstado(dto.getEstado());
        s.setDescripcionEstado(dto.getDescripcion());

        if (dto.getEstado() == SolicitudEstado.PROGRAMADA) {
            log.debug("Programando ruta para solicitud {}", id);
        }

        if (dto.getEstado() == SolicitudEstado.ENTREGADA) {
            log.debug("Calculando valores reales para solicitud {}", id);
        }

        Solicitud saved = solicitudRepository.save(s);
        log.info("Estado de solicitud {} actualizado a {}", saved.getIdSolicitud(), saved.getEstado());
        return new SolicitudResponseDTO(saved.getIdSolicitud(), "Estado actualizado");
    }
}
