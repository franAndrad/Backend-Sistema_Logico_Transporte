package ar.edu.utn.frc.backend.logistica.ms_cliente.service;

import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.cliente.ClienteCreateDTO;
import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.cliente.ClienteDetailsDTO;
import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.cliente.ClienteListDTO;
import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.cliente.ClienteResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.cliente.ClienteUpdateDTO;
import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.Cliente;
import ar.edu.utn.frc.backend.logistica.ms_cliente.repository.ClienteRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private Authentication authentication; // Mock para el parámetro de seguridad

    @InjectMocks
    private ClienteService clienteService;

    private Cliente setupValidCliente(int id) {
        Cliente c = new Cliente();
        c.setIdCliente(id);
        c.setActivo(true);
        c.setKeycloakId("kcid-" + id);
        c.setRazonSocial("Test S.A. " + id);
        c.setCuit("30" + id);
        c.setDireccionFacturacion("Dir F");
        c.setDireccionEnvio("Dir E");
        return c;
    }

    @Test
    void listAll_returnsActiveClients() {
        Cliente c1 = setupValidCliente(1);
        Cliente c2 = setupValidCliente(2);
        when(clienteRepository.findByActivoTrue()).thenReturn(List.of(c1, c2));

        List<ClienteListDTO> result = clienteService.listAll();

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        verify(clienteRepository).findByActivoTrue();
    }

    @Test
    void getById_foundActive_returnsDetails() {
        Cliente c = setupValidCliente(5);
        when(clienteRepository.findByIdClienteAndActivoTrue(5)).thenReturn(Optional.of(c));

        ClienteDetailsDTO result = clienteService.getById(5, authentication);

        assertNotNull(result);
        assertEquals("Test S.A. 5", result.getRazonSocial());
    }

    @Test
    void getById_notFoundOrInactive_throws() {
        when(clienteRepository.findByIdClienteAndActivoTrue(99)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> clienteService.getById(99, authentication));
    }

    @Test
    void create_success() {
        ClienteCreateDTO dto = new ClienteCreateDTO("kcid-new", "Dir F", "Dir E", "New S.A.", "30NEW");
        Cliente saved = setupValidCliente(10);

        when(clienteRepository.existsByKeycloakId("kcid-new")).thenReturn(false);
        when(clienteRepository.existsByCuit("30NEW")).thenReturn(false);
        when(clienteRepository.save(Mockito.isA(Cliente.class))).thenReturn(saved);

        ClienteResponseDTO resp = clienteService.create(dto);

        assertNotNull(resp);
        assertEquals(10, resp.getIdCliente());
        assertEquals("Cliente creado correctamente", resp.getMensaje());
        verify(clienteRepository).save(Mockito.isA(Cliente.class));
    }

    @Test
    void create_duplicateKeycloakId_throws() {
        ClienteCreateDTO dto = new ClienteCreateDTO("kcid-dup", "Dir F", "Dir E", "Dup S.A.", "30DUP");
        when(clienteRepository.existsByKeycloakId("kcid-dup")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> clienteService.create(dto));
        verify(clienteRepository, never()).save(Mockito.isA(Cliente.class));
    }

    @Test
    void update_success() {
        Cliente existing = setupValidCliente(7);
        ClienteUpdateDTO dto = new ClienteUpdateDTO("Dir F N", "Dir E N", "New RS", existing.getCuit()); // Mismo CUIT

        when(clienteRepository.findByIdClienteAndActivoTrue(7)).thenReturn(Optional.of(existing));
        when(clienteRepository.save(Mockito.isA(Cliente.class))).thenReturn(existing);

        ClienteResponseDTO resp = clienteService.update(7, dto, authentication);

        assertEquals(7, resp.getIdCliente());
        assertEquals("New RS", existing.getRazonSocial());
        verify(clienteRepository).save(existing);
    }

    @Test
    void update_newCuitIsDuplicate_throws() {
        Cliente existing = setupValidCliente(7); existing.setCuit("307");
        ClienteUpdateDTO dto = new ClienteUpdateDTO("Dir F N", "Dir E N", "New RS", "30DUP");

        when(clienteRepository.findByIdClienteAndActivoTrue(7)).thenReturn(Optional.of(existing));
        when(clienteRepository.existsByCuit("30DUP")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> clienteService.update(7, dto, authentication));
        verify(clienteRepository, never()).save(Mockito.isA(Cliente.class));
    }

    @Test
    void delete_success_setsActivoFalse() {
        Cliente existing = setupValidCliente(9);
        when(clienteRepository.findByIdClienteAndActivoTrue(9)).thenReturn(Optional.of(existing));
        when(clienteRepository.save(Mockito.isA(Cliente.class))).thenReturn(existing);

        ClienteResponseDTO resp = clienteService.delete(9);

        assertEquals(9, resp.getIdCliente());
        assertFalse(existing.getActivo());
        verify(clienteRepository).save(existing);
    }
}