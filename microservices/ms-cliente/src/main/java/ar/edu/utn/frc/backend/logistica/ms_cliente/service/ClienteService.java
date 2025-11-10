package ar.edu.utn.frc.backend.logistica.ms_cliente.service;

import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.*;
import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.Cliente;
import ar.edu.utn.frc.backend.logistica.ms_cliente.repository.ClienteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<ClienteListDTO> listAll() {
        return clienteRepository.findAll().stream()
                .map(c -> new ClienteListDTO(
                        c.getIdCliente(),
                        c.getNombre(),
                        c.getEmail(),
                        c.getTelefono(),
                        c.getActivo()
                ))
                .collect(Collectors.toList());
    }

    public ClienteDetailsDTO getById(Integer id, Authentication auth) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        // Validar que CLIENTE solo acceda a su propia información
      //  if (isCliente(auth) && !isOwnResource(auth, cliente)) {
       //     throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permiso para acceder a este recurso");
       // }

        return new ClienteDetailsDTO(
                cliente.getIdCliente(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getEmail(),
                cliente.getDireccionFacturacion(),
                cliente.getRazonSocial(),
                cliente.getCuit()
        );
    }

    public ClienteResponseDTO create(ClienteCreateDTO dto) {
        // Validar email único
      //  if (clienteRepository.findByEmail(dto.getEmail()).isPresent()) {
      //      throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
      //  }

        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefono(dto.getTelefono());
        cliente.setDireccionFacturacion(dto.getDireccionFacturacion());
        cliente.setActivo(true);

        // TODO: Integrar con Keycloak para crear usuario y obtener keycloakId
        // Por ahora, usar un placeholder
        cliente.setKeycloakId("keycloak-" + System.currentTimeMillis());

        Cliente saved = clienteRepository.save(cliente);
        return new ClienteResponseDTO(saved.getIdCliente(), "Cliente creado correctamente");
    }

    public ClienteResponseDTO update(Integer id, ClienteUpdateDTO dto, Authentication auth) {
        log.debug("Intentando actualizar cliente id={} con dto={}", id, dto);
        try {
            Cliente cliente = clienteRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

            if (isCliente(auth) && !isOwnResource(auth, cliente)) {
                log.warn("Acceso no autorizado a actualización cliente id={} por auth={}", id, auth);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permiso para actualizar este recurso");
            }

            cliente.setTelefono(dto.getTelefono());
            cliente.setDireccionFacturacion(dto.getDireccionFacturacion());
            Cliente saved = clienteRepository.save(cliente);
            log.info("Cliente id={} actualizado correctamente", saved.getIdCliente());
            return new ClienteResponseDTO(saved.getIdCliente(), "Datos actualizados");
        } catch (ResponseStatusException ex) {
            throw ex; // ya controlada
        } catch (Exception ex) {
            log.error("Error inesperado actualizando cliente id={}: {}", id, ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno actualizando cliente");
        }
    }

    public ClienteResponseDTO delete(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        // Soft delete: desactivar en lugar de eliminar
        cliente.setActivo(false);
        clienteRepository.save(cliente);

        return new ClienteResponseDTO(cliente.getIdCliente(), "Cliente eliminado");
    }

    // Métodos auxiliares para validación de roles y ownership
    private boolean isCliente(Authentication auth) {
        return auth != null && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENTE"));
    }

    private boolean isOwnResource(Authentication auth, Cliente cliente) {
        if (auth == null) {
            return false;
        }
        if (auth.getPrincipal() instanceof Jwt jwt) {
            String keycloakId = jwt.getSubject(); // O el claim que uses para identificar al usuario
            return cliente.getKeycloakId().equals(keycloakId);
        }
        return false;
    }
}