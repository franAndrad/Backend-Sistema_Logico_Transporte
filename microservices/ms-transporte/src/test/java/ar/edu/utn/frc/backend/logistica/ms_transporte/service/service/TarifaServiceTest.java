package ar.edu.utn.frc.backend.logistica.ms_transporte.service.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tarifa.TarifaCreateRequestDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tarifa.TarifaCreateResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tarifa.TarifaListItemDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tarifa.TarifaUpdateRequestDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Tarifa;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.TarifaRepository;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.TarifaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TarifaServiceTest {

    @Mock
    private TarifaRepository tarifaRepository;

    @InjectMocks
    private TarifaService tarifaService;

    private Tarifa setupTarifa(int id, boolean activo) {
        Tarifa t = new Tarifa();
        t.setIdTarifa(id);
        t.setConcepto("Tarifa " + id);
        t.setValorBase(10.0f);
        t.setValorPorKm(1.5f);
        t.setValorPorTramo(50.0f);
        t.setValorLitroCombustible(1.0f);
        t.setActivo(activo);
        t.setFechaVigencia(LocalDate.now());
        return t;
    }

    // --- Creación (Lógica de Vigencia) ---

    @Test
    void crear_success_noExistingActiveTariff() {
        TarifaCreateRequestDTO dto = new TarifaCreateRequestDTO("nueva",2.0f, 2.0f,2.0f,5.0f,2.0f,5.0f, LocalDate.now());
        Tarifa saved = setupTarifa(5, true);

        when(tarifaRepository.existsByActivoTrue()).thenReturn(false);
        when(tarifaRepository.save(Mockito.isA(Tarifa.class))).thenReturn(saved);

        TarifaCreateResponseDTO resp = tarifaService.crear(dto);

        assertEquals(5, resp.getIdTarifa());
        verify(tarifaRepository, times(1)).save(Mockito.isA(Tarifa.class));
        verify(tarifaRepository, never()).findByActivoTrue(); // No debería buscar activas si el exists es false
    }

    @Test
    void crear_success_desactivatesExistingTariff() {
        Tarifa oldActive = setupTarifa(1, true);
        Tarifa newTariff = setupTarifa(5, true);
        TarifaCreateRequestDTO dto = new TarifaCreateRequestDTO("nueva",2.0f, 2.0f,2.0f,5.0f,2.0f,5.0f, LocalDate.now());

        when(tarifaRepository.existsByActivoTrue()).thenReturn(true);
        when(tarifaRepository.findByActivoTrue()).thenReturn(List.of(oldActive));
        when(tarifaRepository.save(Mockito.isA(Tarifa.class))).thenReturn(newTariff);

        tarifaService.crear(dto);

        // Verifica que la antigua tarifa fue desactivada y guardada
        assertFalse(oldActive.getActivo());
        verify(tarifaRepository, times(1)).save(oldActive);

        // Verifica que la nueva tarifa fue guardada
        verify(tarifaRepository, times(2)).save(Mockito.isA(Tarifa.class)); // save(oldActive) + save(newTariff)
    }

    // --- Actualización (Lógica de Vigencia) ---

    @Test
    void actualizar_activate_success_deactivatesOthers() {
        Tarifa t1 = setupTarifa(1, true); // Antigua activa
        Tarifa t2 = setupTarifa(2, false); // La que se va a actualizar y activar
        TarifaUpdateRequestDTO dto = new TarifaUpdateRequestDTO(1.0f, 1.0f, 1.0f, 1.0f, 100.0f, 20.0f, LocalDate.now(), true);

        when(tarifaRepository.findById(2)).thenReturn(Optional.of(t2));
        when(tarifaRepository.findByActivoTrue()).thenReturn(List.of(t1));
        when(tarifaRepository.save(any(Tarifa.class))).thenReturn(t2);

        tarifaService.actualizar(2, dto);

        // La antigua (t1) debe ser desactivada
        assertFalse(t1.getActivo());
        verify(tarifaRepository).save(t1);

        // La nueva (t2) debe ser activada y guardada
        assertTrue(t2.getActivo());
        verify(tarifaRepository).save(t2);
    }

    @Test
    void actualizar_deactivate_success() {
        Tarifa t1 = setupTarifa(1, true); // La que se va a actualizar y desactivar
        TarifaUpdateRequestDTO dto = new TarifaUpdateRequestDTO(1.0f, 1.0f, 1.0f, 1.0f, 100.0f, 20.0f, LocalDate.now(), false); // Activo: false

        when(tarifaRepository.findById(1)).thenReturn(Optional.of(t1));

        tarifaService.actualizar(1, dto);

        // La tarifa debe estar inactiva y guardada
        assertFalse(t1.getActivo());
        verify(tarifaRepository).save(t1);

        // No debería llamar a findByActivoTrue porque no se está activando ninguna.
        verify(tarifaRepository, never()).findByActivoTrue();
    }
}