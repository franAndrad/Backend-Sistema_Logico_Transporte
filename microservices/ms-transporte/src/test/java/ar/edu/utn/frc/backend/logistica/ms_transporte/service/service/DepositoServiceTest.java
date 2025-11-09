package ar.edu.utn.frc.backend.logistica.ms_transporte.service.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.deposito.DepositoCercanoResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.deposito.DepositoCreateRequestDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.deposito.DepositoCreateResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.deposito.DepositoResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.deposito.DepositoUpdateResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Deposito;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.DepositoRepository;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.DepositoService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class DepositoServiceTest {

    @Mock
    private DepositoRepository depositoRepository;

    @InjectMocks
    private DepositoService depositoService;

    @Test
    void findAll_returnsRepositoryList() {
        Deposito d1 = new Deposito(); d1.setIdDeposito(1);
        Deposito d2 = new Deposito(); d2.setIdDeposito(2);
        when(depositoRepository.findAll()).thenReturn(List.of(d1, d2));
        List<Deposito> result = depositoService.findAll();
        assertEquals(2, result.size());
        verify(depositoRepository).findAll();
    }

    @Test
    void findById_found() {
        Deposito d = new Deposito(); d.setIdDeposito(5);
        when(depositoRepository.findById(5)).thenReturn(Optional.of(d));
        Deposito result = depositoService.findById(5);
        assertNotNull(result);
        assertEquals(5, result.getIdDeposito());
    }

    @Test
    void findById_notFound_throws() {
        when(depositoRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> depositoService.findById(99));
    }

    @Test
    void buscarDepositosCercanos_sortsByDistance() {
        Deposito a = new Deposito(); a.setIdDeposito(1); a.setNombre("A"); a.setLatitud(BigDecimal.valueOf(-34.0)); a.setLongitud(BigDecimal.valueOf(-58.0)); a.setActivo(true);
        Deposito b = new Deposito(); b.setIdDeposito(2); b.setNombre("B"); b.setLatitud(BigDecimal.valueOf(-34.1)); b.setLongitud(BigDecimal.valueOf(-58.1)); b.setActivo(true);
        when(depositoRepository.findByActivoTrue()).thenReturn(List.of(b, a));
        List<DepositoCercanoResponseDTO> res = depositoService.buscarDepositosCercanos(-34.0, -58.0);
        assertFalse(res.isEmpty());
        assertEquals(1, res.get(0).getId());
        assertTrue(res.get(0).getDistanciaKm() <= res.get(1).getDistanciaKm());
    }

    @Test
    void save_success() {
        DepositoCreateRequestDTO dto = new DepositoCreateRequestDTO();
        dto.setNombre("X"); dto.setDireccion("Calle 1"); dto.setLatitud(BigDecimal.valueOf(-34.0)); dto.setLongitud(BigDecimal.valueOf(-58.0)); dto.setCostoEstadiaDiario(BigDecimal.TEN);
        when(depositoRepository.existsByNombre("X")).thenReturn(false);
        when(depositoRepository.existsByDireccion("Calle 1")).thenReturn(false);
        Deposito saved = new Deposito(); saved.setIdDeposito(10);
        when(depositoRepository.save(Mockito.isA(Deposito.class))).thenReturn(saved);
        DepositoCreateResponseDTO resp = depositoService.save(dto);
        assertNotNull(resp);
        assertEquals(10, resp.getIdDeposito());
        verify(depositoRepository).save(Mockito.isA(Deposito.class));
    }

    @Test
    void save_duplicateName_throws() {
        DepositoCreateRequestDTO dto = new DepositoCreateRequestDTO(); dto.setNombre("Dup"); dto.setDireccion("C");
        when(depositoRepository.existsByNombre("Dup")).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> depositoService.save(dto));
        verify(depositoRepository, never()).save(Mockito.isA(Deposito.class));
    }

    @Test
    void actualizarDeposito_success() {
        Deposito existing = new Deposito(); existing.setIdDeposito(7); existing.setActivo(true);
        when(depositoRepository.findById(7)).thenReturn(Optional.of(existing));
        DepositoUpdateResponseDTO dto = new DepositoUpdateResponseDTO(); dto.setNombre("N"); dto.setDireccion("D"); dto.setLatitud(BigDecimal.ONE); dto.setLongitud(BigDecimal.ONE); dto.setCostoEstadiaDiario(BigDecimal.ONE);
        DepositoResponseDTO resp = depositoService.actualizarDeposito(7, dto);
        assertEquals(7, resp.getId());
        verify(depositoRepository).save(existing);
        }

    @Test
    void actualizarDeposito_inactive_throws() {
        Deposito existing = new Deposito(); existing.setIdDeposito(8); existing.setActivo(false);
        when(depositoRepository.findById(8)).thenReturn(Optional.of(existing));
        DepositoUpdateResponseDTO dto = new DepositoUpdateResponseDTO();
        assertThrows(IllegalStateException.class, () -> depositoService.actualizarDeposito(8, dto));
    }

    @Test
    void desactivar_and_activar_behaviour() {
        Deposito d = new Deposito(); d.setIdDeposito(20); d.setActivo(true);
        when(depositoRepository.findById(20)).thenReturn(Optional.of(d));
        DepositoResponseDTO resp = depositoService.desactivar(20);
        assertEquals(20, resp.getId());
        assertFalse(d.getActivo());
        verify(depositoRepository, times(1)).save(d);

        
        when(depositoRepository.findById(20)).thenReturn(Optional.of(d));
        DepositoResponseDTO r2 = depositoService.activar(20);
        assertTrue(d.getActivo());
        assertEquals(20, r2.getId());
    }
}
