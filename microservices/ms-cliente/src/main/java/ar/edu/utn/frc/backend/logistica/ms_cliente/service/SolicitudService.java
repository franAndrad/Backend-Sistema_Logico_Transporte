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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.Objects;
import java.math.BigDecimal;
import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.cliente.ClienteDetailsDTO;
import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.contenedor.ContenedorSummaryDTO;
import ar.edu.utn.frc.backend.logistica.ms_cliente.client.transporte.TransporteClient;
import ar.edu.utn.frc.backend.logistica.ms_cliente.client.transporte.dto.*;

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

    public SolicitudDetailsDTO getById(int id) {
        Solicitud s = solicitudRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada"));

        Cliente c = s.getCliente();
        Contenedor cont = s.getContenedor();

        ClienteDetailsDTO clienteDTO = new ClienteDetailsDTO(
                c.getIdCliente(),
                c.getKeycloakId(),
                c.getDireccionFacturacion(),
                c.getDireccionEnvio(),
                c.getRazonSocial(),
                c.getCuit()
        );
        ContenedorSummaryDTO contResumen = new ContenedorSummaryDTO(
                cont.getIdContenedor(),
                cont.getIdentificacion()
        );

        return new SolicitudDetailsDTO(
                s.getIdSolicitud(),
                s.getOrigenDireccion(),
                s.getDestinoDireccion(),
                s.getEstado(),
                s.getCostoEstimado(),
                s.getTiempoEstimado(),
                cont.getIdentificacion(),
                clienteDTO,
                contResumen
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

        // Si pasa a PROGRAMADA, crear Ruta en ms-transporte y calcular costoEstimado
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

            // Obtener ruta: si tenemos idRuta de creación la consultamos por id; si no, por idSolicitud
            RutaDto ruta;
            try {
                if (idRutaCreada != null) {
                    // No hay endpoint directo por id en el cliente; usamos por solicitud como fallback común
                    ruta = transporteClient.obtenerRutaPorSolicitud(s.getIdSolicitud());
                } else {
                    ruta = transporteClient.obtenerRutaPorSolicitud(s.getIdSolicitud());
                }
            } catch (RuntimeException ex) {
                throw new IllegalStateException("No fue posible obtener la ruta para la solicitud " + s.getIdSolicitud());
            }

            // Obtener tramos y sumar costos aproximados
            try {
                List<TramoDto> tramos = transporteClient.obtenerTramosPorRuta(ruta.getIdRuta());
                BigDecimal total = tramos == null ? BigDecimal.ZERO : tramos.stream()
                        .map(t -> t.getCostoAproximado() == null ? BigDecimal.ZERO : t.getCostoAproximado())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                s.setCostoEstimado(total.doubleValue());
            } catch (RuntimeException ex) {
                log.warn("No fue posible calcular el costo estimado desde tramos: {}", ex.getMessage());
            }
        }

        Solicitud saved = solicitudRepository.save(s);
        return new SolicitudResponseDTO(saved.getIdSolicitud(), "Estado actualizado");
    }
}
