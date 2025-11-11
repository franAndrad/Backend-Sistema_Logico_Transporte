package ar.edu.utn.frc.backend.logistica.ms_cliente.service;

import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.solicitud.*;
import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.Cliente;
import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.Contenedor;
import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.Solicitud;
import ar.edu.utn.frc.backend.logistica.ms_cliente.repository.ClienteRepository;
import ar.edu.utn.frc.backend.logistica.ms_cliente.repository.ContenedorRepository;
import ar.edu.utn.frc.backend.logistica.ms_cliente.repository.SolicitudRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final ClienteRepository clienteRepository;
    private final ContenedorRepository contenedorRepository;

    public SolicitudService(SolicitudRepository solicitudRepository,
                            ClienteRepository clienteRepository,
                            ContenedorRepository contenedorRepository) {
        this.solicitudRepository = solicitudRepository;
        this.clienteRepository = clienteRepository;
        this.contenedorRepository = contenedorRepository;
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

    public SolicitudDetailsDTO getById(Integer id) {
        Solicitud s = solicitudRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada"));
        return new SolicitudDetailsDTO(
                s.getIdSolicitud(),
                s.getOrigenDireccion(),
                s.getDestinoDireccion(),
                s.getEstado(),
                s.getCostoEstimado(),
                s.getTiempoEstimado(),
                s.getContenedor().getIdentificacion()
        );
    }

    public List<SolicitudListDTO> listByCliente(Integer clienteId) {
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

    public SolicitudEstadoDTO getEstado(Integer id) {
        Solicitud s = solicitudRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada"));
        return new SolicitudEstadoDTO(s.getIdSolicitud(), s.getEstado(), s.getFechaActualizacion(), s.getDescripcionEstado());
    }

    public SolicitudResponseDTO create(SolicitudCreateDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.getIdCliente())
                .orElseThrow(() -> new NoSuchElementException("Cliente no encontrado"));
        Contenedor contenedor = contenedorRepository.findById(dto.getIdContenedor())
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

    public SolicitudResponseDTO update(Integer id, SolicitudUpdateDTO dto) {
        Solicitud s = solicitudRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada"));
        s.setEstado(dto.getEstado());
        s.setTarifaId(dto.getTarifaId());
        Solicitud saved = solicitudRepository.save(s);
        return new SolicitudResponseDTO(saved.getIdSolicitud(), "Solicitud actualizada");
    }

    public SolicitudResponseDTO updateEstado(Integer id, SolicitudEstadoUpdateDTO dto) {
        Solicitud s = solicitudRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada"));
        s.setEstado(dto.getEstado());
        s.setDescripcionEstado(dto.getDescripcion());
        Solicitud saved = solicitudRepository.save(s);
        return new SolicitudResponseDTO(saved.getIdSolicitud(), "Estado actualizado");
    }
}
