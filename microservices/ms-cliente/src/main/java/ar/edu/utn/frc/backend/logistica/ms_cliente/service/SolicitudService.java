package ar.edu.utn.frc.backend.logistica.ms_cliente.service;

import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.solicitud.*;
import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.Cliente;
import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.Contenedor;
import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.Solicitud;
import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.SolicitudEstado;
import ar.edu.utn.frc.backend.logistica.ms_cliente.repository.ClienteRepository;
import ar.edu.utn.frc.backend.logistica.ms_cliente.repository.ContenedorRepository;
import ar.edu.utn.frc.backend.logistica.ms_cliente.repository.SolicitudRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import java.time.Duration;

import ar.edu.utn.frc.backend.logistica.ms_cliente.client.transporte.TransporteClient;
import ar.edu.utn.frc.backend.logistica.ms_cliente.client.transporte.dto.*;
import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.cliente.ClienteDetailsDTO;
import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.contenedor.ContenedorSummaryDTO;

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
        return solicitudRepository.findAll().stream()
                .map(s -> new SolicitudListDTO(
                        s.getIdSolicitud(),
                        s.getCliente().getIdCliente(),
                        s.getEstado(),
                        s.getCostoEstimado(),
                        s.getFechaCreacion()
                ))
                .collect(Collectors.toList());
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
        // Redondear hacia arriba a minutos para no perder tramos cortos (<1 min)
        int minutes = (int) Math.ceil(totalSeconds / 60.0);
        return Math.max(minutes, 1);
    }

    public SolicitudDetailsDTO getById(int id) {
        Solicitud s = solicitudRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada"));

        // Fallback: si tiempoEstimado es null, intentar calcularlo desde tramos
        if (s.getTiempoEstimado() == null) {
            try {
                RutaDto rutaDtoTmp = transporteClient.obtenerRutaPorSolicitud(s.getIdSolicitud());
                if (rutaDtoTmp != null && rutaDtoTmp.getIdRuta() != null) {
                    List<TramoDto> tramosTmp = transporteClient.obtenerTramosPorRuta(rutaDtoTmp.getIdRuta());
                    Integer mins = calcularTiempoEstimadoMin(tramosTmp);
                    if (mins != null) {
                        s.setTiempoEstimado(mins);
                        solicitudRepository.save(s);
                    }
                }
            } catch (RuntimeException ex) {
                log.warn("No fue posible backfillear tiempoEstimado desde tramos en getById: {}", ex.getMessage());
            }
        }

        // Obtener ruta/tramos para adjuntar y (si ENTREGADA y faltan) backfillear costoFinal/tiempoReal
        RutaDto rutaDto = null;
        List<TramoDto> tramosDto = null;
        try {
            rutaDto = transporteClient.obtenerRutaPorSolicitud(s.getIdSolicitud());
            if (rutaDto != null && rutaDto.getIdRuta() != null) {
                tramosDto = transporteClient.obtenerTramosPorRuta(rutaDto.getIdRuta());
                if (s.getEstado() == SolicitudEstado.ENTREGADA) {
                    // Backfill valores reales si faltan en la entidad
                    if (s.getCostoFinal() == null) {
                        BigDecimal totalReal = tramosDto.stream()
                                .map(t -> t.getCostoReal() == null ? BigDecimal.ZERO : t.getCostoReal())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        s.setCostoFinal(totalReal.doubleValue());
                    }
                    if (s.getTiempoReal() == null) {
                        Integer minsReal = calcularTiempoRealMin(tramosDto);
                        if (minsReal != null) s.setTiempoReal(minsReal);
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
        return solicitudRepository.findByCliente_IdCliente(clienteId).stream()
                .map(s -> new SolicitudListDTO(
                        s.getIdSolicitud(),
                        s.getCliente().getIdCliente(),
                        s.getEstado(),
                        s.getCostoEstimado(),
                        s.getFechaCreacion()
                ))
                .collect(Collectors.toList());
    }

    public SolicitudEstadoDTO getEstado(int id) {
        Solicitud s = solicitudRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada"));
        return new SolicitudEstadoDTO(s.getIdSolicitud(), s.getEstado(), s.getFechaActualizacion(), s.getDescripcionEstado());
    }

    public SolicitudResponseDTO create(SolicitudCreateDTO dto) {
        final Integer idCliente = Objects.requireNonNull(dto.getIdCliente(), "idCliente no puede ser null");
        final Integer idContenedor = Objects.requireNonNull(dto.getIdContenedor(), "idContenedor no puede ser null");


        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new NoSuchElementException("Cliente no encontrado"));
        Contenedor contenedor = contenedorRepository.findById(idContenedor)
                .orElseThrow(() -> new NoSuchElementException("Contenedor no encontrado"));


        if (!contenedor.getCliente().getIdCliente().equals(cliente.getIdCliente())) {
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
        return new SolicitudResponseDTO(saved.getIdSolicitud(), "Solicitud creada (BORRADOR)");
    }

    public SolicitudResponseDTO update(int id, SolicitudUpdateDTO dto) {
        Solicitud s = solicitudRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada"));
        s.setEstado(dto.getEstado());
        s.setTarifaId(dto.getTarifaId());
        Solicitud saved = solicitudRepository.save(s);
        return new SolicitudResponseDTO(saved.getIdSolicitud(), "Solicitud actualizada");
    }

    public SolicitudResponseDTO updateEstado(int id, SolicitudEstadoUpdateDTO dto) {
        Solicitud s = solicitudRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada"));
        s.setEstado(dto.getEstado());
        s.setDescripcionEstado(dto.getDescripcion());

        if (dto.getEstado() == SolicitudEstado.PROGRAMADA) {
            // Validar coordenadas
            if (s.getOrigenLatitud() == null || s.getOrigenLongitud() == null
                    || s.getDestinoLatitud() == null || s.getDestinoLongitud() == null) {
                throw new IllegalStateException("Coordenadas de origen/destino requeridas para crear la ruta");
            }
            RutaCreateRequestDto body = new RutaCreateRequestDto(
                    s.getIdSolicitud(),
                    BigDecimal.valueOf(s.getOrigenLatitud()),
                    BigDecimal.valueOf(s.getOrigenLongitud()),
                    BigDecimal.valueOf(s.getDestinoLatitud()),
                    BigDecimal.valueOf(s.getDestinoLongitud()),
                    null
            );

            Integer idRutaCreada = null;
            try {
                RutaCreateResponseDto resp = transporteClient.crearRuta(body);
                if (resp != null) idRutaCreada = resp.getIdRuta();
            } catch (RuntimeException ex) {
                log.warn("No se pudo crear la ruta (posible existencia previa o error de red): {}", ex.getMessage());
            }

            // Obtener ruta por solicitud
            RutaDto ruta;
            try {
                ruta = transporteClient.obtenerRutaPorSolicitud(s.getIdSolicitud());
            } catch (RuntimeException ex) {
                throw new IllegalStateException("No fue posible obtener la ruta para la solicitud " + s.getIdSolicitud());
            }

            // Obtener tramos, sumar costo y tiempo estimado desde fechas estimadas
            try {
                List<TramoDto> tramos = transporteClient.obtenerTramosPorRuta(ruta.getIdRuta());
                BigDecimal total = tramos == null ? BigDecimal.ZERO : tramos.stream()
                        .map(t -> t.getCostoAproximado() == null ? BigDecimal.ZERO : t.getCostoAproximado())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                s.setCostoEstimado(total.doubleValue());

                Integer mins = calcularTiempoEstimadoMin(tramos);
                if (mins != null) s.setTiempoEstimado(mins);
            } catch (RuntimeException ex) {
                log.warn("No fue posible calcular costo/tiempo estimado desde tramos: {}", ex.getMessage());
            }
        }

        // Al pasar a ENTREGADA, calcular y persistir costoFinal y tiempoReal desde ms-transporte
        if (dto.getEstado() == SolicitudEstado.ENTREGADA) {
            try {
                RutaDto ruta = transporteClient.obtenerRutaPorSolicitud(s.getIdSolicitud());
                if (ruta != null && ruta.getIdRuta() != null) {
                    List<TramoDto> tramos = transporteClient.obtenerTramosPorRuta(ruta.getIdRuta());
                    BigDecimal totalReal = tramos == null ? BigDecimal.ZERO : tramos.stream()
                            .map(t -> t.getCostoReal() == null ? BigDecimal.ZERO : t.getCostoReal())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    s.setCostoFinal(totalReal.doubleValue());
                    Integer minsReal = calcularTiempoRealMin(tramos);
                    if (minsReal != null) s.setTiempoReal(minsReal);
                }
            } catch (RuntimeException ex) {
                log.warn("No fue posible calcular valores reales en ENTREGADA: {}", ex.getMessage());
            }
        }

        Solicitud saved = solicitudRepository.save(s);
        return new SolicitudResponseDTO(saved.getIdSolicitud(), "Estado actualizado");
    }
}
