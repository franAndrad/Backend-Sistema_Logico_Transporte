package ar.edu.utn.frc.backend.logistica.ms_cliente.service;

import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.contenedor.ContenedorCreateDTO;
import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.contenedor.ContenedorDetailsDTO;
import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.contenedor.ContenedorListDTO;
import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.contenedor.ContenedorResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.contenedor.ContenedorUpdateDTO;
import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.Cliente;
import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.Contenedor;
import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.ContenedorEstado;
import ar.edu.utn.frc.backend.logistica.ms_cliente.repository.ClienteRepository;
import ar.edu.utn.frc.backend.logistica.ms_cliente.repository.ContenedorRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContenedorServiceTest {

    @Mock
    private ContenedorRepository contenedorRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ContenedorService contenedorService;

    private Cliente setupValidCliente(int id) {
        Cliente c = new Cliente();
        c.setIdCliente(id);
        c.setKeycloakId("kcid-" + id);
        c.setRazonSocial("Test S.A.");
        c.setCuit("30" + id);
        c.setDireccionFacturacion("Dir F");
        c.setDireccionEnvio("Dir E");
        c.setActivo(true);
        return c;
    }

    private Contenedor setupValidContenedor(int id, Cliente cliente) {
        Contenedor c = new Contenedor();
        c.setIdContenedor(id);
        c.setIdentificacion("C-" + id);
        c.setCliente(cliente);
        c.setPeso(100.0);
        c.setVolumen(50.0);
        c.setEstado(ContenedorEstado.EN_ORIGEN);
        c.setActivo(true);
        return c;
    }

    @Test
    void listAll_returnsActiveContainers() {
        Cliente c1 = setupValidCliente(1);
        Contenedor cont1 = setupValidContenedor(10, c1);
        Contenedor cont2 = setupValidContenedor(11, c1);
        when(contenedorRepository.findByActivoTrue()).thenReturn(List.of(cont1, cont2));

        List<ContenedorListDTO> result = contenedorService.listAll();

        assertEquals(2, result.size());
        assertEquals(10, result.get(0).getId());
        verify(contenedorRepository).findByActivoTrue();
    }

    @Test
    void getById_foundActive_returnsDetails() {
        Cliente cliente = setupValidCliente(1);
        Contenedor cont = setupValidContenedor(15, cliente);
        when(contenedorRepository.findByIdContenedorAndActivoTrue(15)).thenReturn(Optional.of(cont));

        ContenedorDetailsDTO result = contenedorService.getById(15);

        assertNotNull(result);
        assertEquals("C-15", result.getIdentificacion());
        assertEquals(1, result.getCliente().getId()); // Verifica que el DTO del Cliente fue mapeado
    }

    @Test
    void getById_notFoundOrInactive_throws() {
        when(contenedorRepository.findByIdContenedorAndActivoTrue(99)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> contenedorService.getById(99));
    }

    @Test
    void create_success_defaultEstado() {
        ContenedorCreateDTO dto = new ContenedorCreateDTO("NEW-C", 1, 500.0, 10.0, null);
        Cliente cliente = setupValidCliente(1);
        Contenedor saved = setupValidContenedor(20, cliente);

        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(contenedorRepository.existsByIdentificacion("NEW-C")).thenReturn(false);
        when(contenedorRepository.save(Mockito.isA(Contenedor.class))).thenReturn(saved);

        ContenedorResponseDTO resp = contenedorService.create(dto);

        assertNotNull(resp);
        assertEquals(20, resp.getId());
        verify(contenedorRepository).save(Mockito.argThat(c -> c.getEstado() == ContenedorEstado.EN_ORIGEN));
    }

    @Test
    void create_duplicateIdentificacion_throws() {
        ContenedorCreateDTO dto = new ContenedorCreateDTO("DUP-C", 1, 500.0, 10.0, null);
        when(contenedorRepository.existsByIdentificacion("DUP-C")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> contenedorService.create(dto));
        verify(contenedorRepository, never()).save(Mockito.isA(Contenedor.class));
    }

    @Test
    void update_success() {
        Contenedor existing = setupValidContenedor(7, setupValidCliente(1));
        ContenedorUpdateDTO dto = new ContenedorUpdateDTO(1000.0, 20.0, ContenedorEstado.EN_DEPOSITO);

        when(contenedorRepository.findByIdContenedorAndActivoTrue(7)).thenReturn(Optional.of(existing));
        when(contenedorRepository.save(Mockito.isA(Contenedor.class))).thenReturn(existing);

        ContenedorResponseDTO resp = contenedorService.update(7, dto);

        assertEquals(7, resp.getId());
        assertEquals(1000.0, existing.getPeso());
        assertEquals(ContenedorEstado.EN_DEPOSITO, existing.getEstado());
        verify(contenedorRepository).save(existing);
    }
}