package ar.edu.utn.frc.backend.logistica.ms_cliente.service;

import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.contenedor.*;
import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.Cliente;
import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.Contenedor;
import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.ContenedorEstado;
import ar.edu.utn.frc.backend.logistica.ms_cliente.repository.ClienteRepository;
import ar.edu.utn.frc.backend.logistica.ms_cliente.repository.ContenedorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Objects;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.cliente.ClienteDetailsDTO;

@Service
@Slf4j
public class ContenedorService {

    private final ContenedorRepository contenedorRepository;
    private final ClienteRepository clienteRepository;

    public ContenedorService(ContenedorRepository contenedorRepository, ClienteRepository clienteRepository) {
        this.contenedorRepository = contenedorRepository;
        this.clienteRepository = clienteRepository;
    }

    public List<ContenedorListDTO> listAll() {
        log.info("Listando todos los contenedores activos");
        List<ContenedorListDTO> contenedores = contenedorRepository.findByActivoTrue().stream()
                .map(c -> new ContenedorListDTO(
                        c.getIdContenedor(),
                        c.getIdentificacion(),
                        c.getPeso(),
                        c.getVolumen(),
                        c.getEstado(),
                        c.getCliente().getIdCliente()
                ))
                .collect(Collectors.toList());
        log.info("Se encontraron {} contenedores activos", contenedores.size());
        return contenedores;
    }

    public ContenedorDetailsDTO getById(Integer id) {
        log.info("Obteniendo contenedor con id {}", id);
        Contenedor c = contenedorRepository.findByIdContenedorAndActivoTrue(id)
                .orElseThrow(() -> {
                    log.error("Contenedor no encontrado con id {}", id);
                    return new NoSuchElementException("Contenedor no encontrado");
                });
        Cliente cl = c.getCliente();
        ClienteDetailsDTO clienteDTO = new ClienteDetailsDTO(
                cl.getIdCliente(),
                cl.getKeycloakId(),
                cl.getDireccionFacturacion(),
                cl.getDireccionEnvio(),
                cl.getRazonSocial(),
                cl.getCuit()
        );
        log.debug("Contenedor encontrado: {}", c.getIdentificacion());
        return new ContenedorDetailsDTO(
                c.getIdContenedor(),
                c.getIdentificacion(),
                c.getPeso(),
                c.getVolumen(),
                c.getEstado(),
                c.getActivo(),
                clienteDTO
        );
    }

    public List<ContenedorListDTO> listByCliente(Integer clienteId) {
        log.info("Listando contenedores para cliente id {}", clienteId);
        List<ContenedorListDTO> contenedores = contenedorRepository.findByCliente_IdClienteAndActivoTrue(clienteId).stream()
                .map(c -> new ContenedorListDTO(
                        c.getIdContenedor(),
                        c.getIdentificacion(),
                        c.getPeso(),
                        c.getVolumen(),
                        c.getEstado(),
                        c.getCliente().getIdCliente()
                ))
                .collect(Collectors.toList());
        log.info("Se encontraron {} contenedores para cliente id {}", contenedores.size(), clienteId);
        return contenedores;
    }

    public ContenedorResponseDTO create(ContenedorCreateDTO dto) {
        final Integer idCliente = Objects.requireNonNull(dto.getClienteId(), "idCliente no puede ser null");
        log.info("Creando contenedor para cliente id {}, identificacion {}", idCliente, dto.getIdentificacion());

        if (contenedorRepository.existsByIdentificacion(dto.getIdentificacion())) {
            log.error("Identificación de contenedor duplicada: {}", dto.getIdentificacion());
            throw new IllegalStateException("Identificación de contenedor duplicada");
        }

        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> {
                    log.error("Cliente no encontrado con id {}", idCliente);
                    return new NoSuchElementException("Cliente no encontrado");
                });

        Contenedor cont = new Contenedor();
        cont.setIdentificacion(dto.getIdentificacion());
        cont.setCliente(cliente);
        cont.setPeso(dto.getPeso());
        cont.setVolumen(dto.getVolumen());
        cont.setEstado(dto.getEstado() != null ? dto.getEstado() : ContenedorEstado.EN_ORIGEN);
        cont.setActivo(true);

        Contenedor saved = contenedorRepository.save(cont);
        log.info("Contenedor creado correctamente con id {}", saved.getIdContenedor());
        return new ContenedorResponseDTO(saved.getIdContenedor(), "Contenedor registrado");
    }

    public ContenedorResponseDTO update(Integer id, ContenedorUpdateDTO dto) {
        log.info("Actualizando contenedor con id {}", id);
        Contenedor cont = contenedorRepository.findByIdContenedorAndActivoTrue(id)
                .orElseThrow(() -> {
                    log.error("Contenedor no encontrado con id {}", id);
                    return new NoSuchElementException("Contenedor no encontrado");
                });

        cont.setPeso(dto.getPeso());
        cont.setVolumen(dto.getVolumen());
        cont.setEstado(dto.getEstado());

        Contenedor saved = contenedorRepository.save(cont);
        log.info("Contenedor con id {} actualizado correctamente", saved.getIdContenedor());
        return new ContenedorResponseDTO(saved.getIdContenedor(), "Contenedor actualizado");
    }

    public ContenedorResponseDTO delete(Integer id) {
        log.info("Desactivando contenedor con id {}", id);
        Contenedor cont = contenedorRepository.findByIdContenedorAndActivoTrue(id)
                .orElseThrow(() -> {
                    log.error("Contenedor no encontrado con id {}", id);
                    return new NoSuchElementException("Contenedor no encontrado");
                });

        cont.setActivo(false);
        Contenedor saved = contenedorRepository.save(cont);
        log.info("Contenedor con id {} desactivado correctamente", saved.getIdContenedor());
        return new ContenedorResponseDTO(saved.getIdContenedor(), "Contenedor desactivado");
    }
}
