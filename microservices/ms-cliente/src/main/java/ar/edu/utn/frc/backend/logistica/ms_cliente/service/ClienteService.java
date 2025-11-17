package ar.edu.utn.frc.backend.logistica.ms_cliente.service;

import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.cliente.*;
import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.Cliente;
import ar.edu.utn.frc.backend.logistica.ms_cliente.repository.ClienteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<ClienteListDTO> listAll() {
        log.info("Listando todos los clientes activos");
        List<ClienteListDTO> clientes = clienteRepository.findByActivoTrue().stream()
                .map(c -> new ClienteListDTO(
                        c.getIdCliente(),
                        c.getKeycloakId(),
                        c.getRazonSocial(),
                        c.getCuit(),
                        c.getActivo()
                ))
                .collect(Collectors.toList());
        log.info("Se encontraron {} clientes activos", clientes.size());
        return clientes;
    }

    public ClienteDetailsDTO getById(Integer id, Authentication auth) {
        log.info("Obteniendo detalles del cliente con id {}", id);
        Cliente cliente = clienteRepository.findByIdClienteAndActivoTrue(id)
                .orElseThrow(() -> {
                    log.error("Cliente no encontrado con id {}", id);
                    return new NoSuchElementException("Cliente no encontrado");
                });

        log.debug("Cliente encontrado: {}", cliente);
        return new ClienteDetailsDTO(
                cliente.getIdCliente(),
                cliente.getKeycloakId(),
                cliente.getDireccionFacturacion(),
                cliente.getDireccionEnvio(),
                cliente.getRazonSocial(),
                cliente.getCuit()
        );
    }

    public ClienteResponseDTO create(ClienteCreateDTO dto) {
        log.info("Creando cliente con KeycloakId {} y CUIT {}", dto.getKeycloakId(), dto.getCuit());

        if (clienteRepository.existsByKeycloakId(dto.getKeycloakId())) {
            log.error("Ya existe un cliente para el usuario Keycloak {}", dto.getKeycloakId());
            throw new IllegalStateException("Ya existe un cliente para ese usuario Keycloak");
        }
        if (dto.getCuit() != null && !dto.getCuit().isBlank() && clienteRepository.existsByCuit(dto.getCuit())) {
            log.error("Ya existe un cliente con CUIT {}", dto.getCuit());
            throw new IllegalStateException("Ya existe un cliente con ese CUIT");
        }

        Cliente cliente = new Cliente();
        cliente.setKeycloakId(dto.getKeycloakId());
        cliente.setDireccionFacturacion(dto.getDireccionFacturacion());
        cliente.setDireccionEnvio(dto.getDireccionEnvio());
        cliente.setRazonSocial(dto.getRazonSocial());
        cliente.setCuit(dto.getCuit());
        cliente.setActivo(true);

        Cliente saved = clienteRepository.save(cliente);
        log.info("Cliente creado correctamente con id {}", saved.getIdCliente());
        return new ClienteResponseDTO(saved.getIdCliente(), "Cliente creado correctamente");
    }

    public ClienteResponseDTO update(Integer id, ClienteUpdateDTO dto, Authentication auth) {
        log.info("Actualizando cliente con id {}", id);
        Cliente cliente = clienteRepository.findByIdClienteAndActivoTrue(id)
                .orElseThrow(() -> {
                    log.error("Cliente no encontrado con id {}", id);
                    return new NoSuchElementException("Cliente no encontrado");
                });

        if (dto.getCuit() != null && !dto.getCuit().isBlank()) {
            String nuevoCuit = dto.getCuit();
            String actualCuit = cliente.getCuit();
            if ((actualCuit == null || !actualCuit.equals(nuevoCuit)) && clienteRepository.existsByCuit(nuevoCuit)) {
                log.error("Ya existe un cliente con CUIT {}", nuevoCuit);
                throw new IllegalStateException("Ya existe un cliente con ese CUIT");
            }
            cliente.setCuit(nuevoCuit);
        }

        cliente.setDireccionFacturacion(dto.getDireccionFacturacion());
        cliente.setDireccionEnvio(dto.getDireccionEnvio());
        cliente.setRazonSocial(dto.getRazonSocial());

        Cliente saved = clienteRepository.save(cliente);
        log.info("Cliente con id {} actualizado correctamente", saved.getIdCliente());
        return new ClienteResponseDTO(saved.getIdCliente(), "Datos actualizados");
    }

    public ClienteResponseDTO delete(Integer id) {
        log.info("Eliminando (inactivando) cliente con id {}", id);
        Cliente cliente = clienteRepository.findByIdClienteAndActivoTrue(id)
                .orElseThrow(() -> {
                    log.error("Cliente no encontrado con id {}", id);
                    return new NoSuchElementException("Cliente no encontrado");
                });

        cliente.setActivo(false);
        clienteRepository.save(cliente);
        log.info("Cliente con id {} eliminado correctamente", cliente.getIdCliente());

        return new ClienteResponseDTO(cliente.getIdCliente(), "Cliente eliminado");
    }
}
