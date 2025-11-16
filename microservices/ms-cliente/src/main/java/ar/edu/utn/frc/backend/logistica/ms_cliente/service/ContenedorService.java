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
        return contenedorRepository.findByActivoTrue().stream()
                .map(c -> new ContenedorListDTO(
                        c.getIdContenedor(),
                        c.getIdentificacion(),
                        c.getPeso(),
                        c.getVolumen(),
                        c.getEstado(),
                        c.getCliente().getIdCliente()
                ))
                .collect(Collectors.toList());
    }

    public ContenedorDetailsDTO getById(Integer id) {
        Contenedor c = contenedorRepository.findByIdContenedorAndActivoTrue(id)
                .orElseThrow(() -> new NoSuchElementException("Contenedor no encontrado"));
        Cliente cl = c.getCliente();
        ClienteDetailsDTO clienteDTO = new ClienteDetailsDTO(
                cl.getIdCliente(),
                cl.getKeycloakId(),
                cl.getDireccionFacturacion(),
                cl.getDireccionEnvio(),
                cl.getRazonSocial(),
                cl.getCuit()
        );
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
        return contenedorRepository.findByCliente_IdClienteAndActivoTrue(clienteId).stream()
                .map(c -> new ContenedorListDTO(
                        c.getIdContenedor(),
                        c.getIdentificacion(),
                        c.getPeso(),
                        c.getVolumen(),
                        c.getEstado(),
                        c.getCliente().getIdCliente()
                ))
                .collect(Collectors.toList());
    }

    public ContenedorResponseDTO create(ContenedorCreateDTO dto) {
        
        final Integer idCliente = Objects.requireNonNull(dto.getClienteId(), "idCliente no puede ser null");
        
        if (contenedorRepository.existsByIdentificacion(dto.getIdentificacion())) {
            throw new IllegalStateException("Identificación de contenedor duplicada");
        }

        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new NoSuchElementException("Cliente no encontrado"));

        Contenedor cont = new Contenedor();
        cont.setIdentificacion(dto.getIdentificacion());
        cont.setCliente(cliente);
        cont.setPeso(dto.getPeso());
        cont.setVolumen(dto.getVolumen());
        cont.setEstado(dto.getEstado() != null ? dto.getEstado() : ContenedorEstado.EN_ORIGEN);
        cont.setActivo(true);
        Contenedor saved = contenedorRepository.save(cont);
        return new ContenedorResponseDTO(saved.getIdContenedor(), "Contenedor registrado");
    }

    public ContenedorResponseDTO update(Integer id, ContenedorUpdateDTO dto) {
        Contenedor cont = contenedorRepository.findByIdContenedorAndActivoTrue(id)
                .orElseThrow(() -> new NoSuchElementException("Contenedor no encontrado"));
        cont.setPeso(dto.getPeso());
        cont.setVolumen(dto.getVolumen());
        cont.setEstado(dto.getEstado());
        Contenedor saved = contenedorRepository.save(cont);
        return new ContenedorResponseDTO(saved.getIdContenedor(), "Contenedor actualizado");
    }

    public ContenedorResponseDTO delete(Integer id) {
        Contenedor cont = contenedorRepository.findByIdContenedorAndActivoTrue(id)
                .orElseThrow(() -> new NoSuchElementException("Contenedor no encontrado"));
        cont.setActivo(false);
        Contenedor saved = contenedorRepository.save(cont);
        return new ContenedorResponseDTO(saved.getIdContenedor(), "Contenedor desactivado");
    }
}
