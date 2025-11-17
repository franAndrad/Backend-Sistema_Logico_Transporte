package ar.edu.utn.frc.backend.logistica.ms_transporte.service.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.camion.CamionCreateRequestDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.camion.CamionCreateResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.camion.CamionResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.camion.CamionUpdateRequestDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Camion;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.CamionRepository;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.CamionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CamionServiceTest {

    @Mock
    private CamionRepository camionRepository;

    @InjectMocks
    private CamionService camionService;

    private Camion setupCamion(String dominio, boolean disponible) {
        Camion c = new Camion();
        c.setDominio(dominio);
        c.setCapacidad(1.456F);
        c.setVolumen(50.0F);
        c.setCostoBase(new BigDecimal(0.08));
        c.setConsumoCombustible(0.2F);
        c.setDisponibilidad(disponible);
        return c;
    }

    // --- Listado y Búsqueda ---

    @Test
    void findAll_returnsAllCamiones() {
        Camion c1 = setupCamion("AAA111", true);
        Camion c2 = setupCamion("BBB222", false);
        when(camionRepository.findAll()).thenReturn(List.of(c1, c2));

        List<Camion> result = camionService.findAll();
        assertEquals(2, result.size());
        verify(camionRepository).findAll();
    }

    @Test
    void findAllDisponibles_returnsOnlyAvailable() {
        Camion c1 = setupCamion("AAA111", true);
        Camion c2 = setupCamion("BBB222", false);
        when(camionRepository.findByDisponibilidadTrue()).thenReturn(List.of(c1));

        List<Camion> result = camionService.findAllDisponibles();
        assertEquals(1, result.size());
        assertEquals("AAA111", result.get(0).getDominio());
        verify(camionRepository).findByDisponibilidadTrue();
    }

    @Test
    void findByDominio_found() {
        Camion c = setupCamion("AAA111", true);
        when(camionRepository.findById("AAA111")).thenReturn(Optional.of(c));

        Camion result = camionService.findByDominio("AAA111");
        assertNotNull(result);
        assertEquals("AAA111", result.getDominio());
    }

    @Test
    void findByDominio_notFound_throws() {
        when(camionRepository.findById("XXX")).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> camionService.findByDominio("XXX"));
    }

    // --- Creación ---

    @Test
    void save_success() {
        CamionCreateRequestDTO dto = new CamionCreateRequestDTO("CCC333", 2000.0f, 60.0f, new BigDecimal(0.45), 0.3f);
        Camion saved = setupCamion("CCC333", true);

        when(camionRepository.existsByDominio("CCC333")).thenReturn(false);
        when(camionRepository.save(Mockito.isA(Camion.class))).thenReturn(saved);

        Camion result = camionService.save(dto);

        assertNotNull(result);
        assertTrue(result.getDisponibilidad());
        assertEquals(6.0, result.getCostoBase());
        verify(camionRepository).save(Mockito.isA(Camion.class));
    }

    @Test
    void save_duplicateDominio_throws() {
        CamionCreateRequestDTO dto = new CamionCreateRequestDTO("CCC333", 2000.0f, 60.0f, new BigDecimal(0.45), 0.3f);
        when(camionRepository.existsByDominio("DUP")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> camionService.save(dto));
        verify(camionRepository, never()).save(Mockito.isA(Camion.class));
    }

    // --- Actualización ---



    @Test
    void deshabilitar_success_wasAvailable() {
        Camion existing = setupCamion("AAA111", true);
        when(camionRepository.findById("AAA111")).thenReturn(Optional.of(existing));
        when(camionRepository.save(existing)).thenReturn(existing);

        CamionResponseDTO resp = camionService.deshabilitar("AAA111");

        assertFalse(existing.getDisponibilidad());
        assertEquals("Camión deshabilitado correctamente", resp.getMensaje());
        verify(camionRepository).save(existing);
    }

    @Test
    void deshabilitar_alreadyUnavailable() {
        Camion existing = setupCamion("BBB222", false);
        when(camionRepository.findById("BBB222")).thenReturn(Optional.of(existing));

        CamionResponseDTO resp = camionService.deshabilitar("BBB222");

        assertEquals("El camión ya estaba deshabilitado", resp.getMensaje());
        verify(camionRepository, never()).save(existing);
    }
}