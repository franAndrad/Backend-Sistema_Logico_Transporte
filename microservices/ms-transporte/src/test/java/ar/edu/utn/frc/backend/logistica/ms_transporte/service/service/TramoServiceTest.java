package ar.edu.utn.frc.backend.logistica.ms_transporte.service.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tramo.*;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Camion;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Ruta;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Tarifa;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Tramo;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.enums.EstadoTramo;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.enums.TipoTramo;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.RutaRepository;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.TarifaRepository;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.TramoRepository;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.CamionService;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.RutaService;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.TramoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TramoServiceTest {

    @Mock private TramoRepository tramoRepository;
    @Mock private RutaRepository rutaRepository;
    @Mock private CamionService camionService;
    @Mock private RutaService rutaService;
    @Mock private TarifaRepository tarifaRepository;

    @InjectMocks
    private TramoService tramoService;

    private Ruta setupRuta(int id) {
        Ruta r = new Ruta(); r.setIdRuta(id); return r;
    }

    private Tramo setupTramo(int id, EstadoTramo estado, Ruta ruta, String camion, String transportista) {
        Tramo t = new Tramo();
        t.setIdTramo(id);
        t.setRuta(ruta);
        t.setEstado(estado);
        t.setDominioCamion(camion);
        t.setKeyCloakIdTransportista(transportista);
        t.setFechaHoraInicio(LocalDateTime.now().minusHours(1));
        t.setDistancia(BigDecimal.valueOf(100.0));
        return t;
    }

    private Tarifa setupTarifaVigente() {
        Tarifa t = new Tarifa();
        t.setValorPorTramo(10.0f);
        t.setValorLitroCombustible(100.0f);
        return t;
    }

    private Camion setupCamion(String dominio, double costoBase, float consumo) {
        Camion c = new Camion();
        c.setDominio(dominio);
        c.setCostoBase(new BigDecimal(costoBase));
        c.setConsumoCombustible(consumo);
        c.setDisponibilidad(true);
        return c;
    }

    // --- Creación, Actualización, Eliminación ---

    @Test
    void crear_success() {
        Ruta ruta = setupRuta(1);
        TramoCreateRequestDTO dto = new TramoCreateRequestDTO(1, null, null, TipoTramo.ORIGEN_DEPOSITO, BigDecimal.TEN, BigDecimal.valueOf(50.0), null, null);
        Tramo saved = setupTramo(10, EstadoTramo.PLANIFICADO, ruta, null, null);

        when(rutaRepository.findById(1)).thenReturn(Optional.of(ruta));
        when(tramoRepository.save(Mockito.isA(Tramo.class))).thenReturn(saved);

        TramoCreateResponseDTO resp = tramoService.crear(dto);

        assertEquals(10, resp.getIdTramo());
        verify(tramoRepository).save(Mockito.isA(Tramo.class));
        verify(rutaService).recalcularDesdeTramos(1);
    }

    @Test
    void eliminar_tramoPlanificado_success() {
        Ruta ruta = setupRuta(1);
        Tramo tramo = setupTramo(10, EstadoTramo.PLANIFICADO, ruta, null, null);

        when(tramoRepository.findById(10)).thenReturn(Optional.of(tramo));

        tramoService.eliminar(10);

        verify(tramoRepository).delete(tramo);
        verify(rutaService).recalcularDesdeTramos(1);
    }

    @Test
    void eliminar_tramoIniciado_throws() {
        Tramo tramo = setupTramo(10, EstadoTramo.INICIADO, setupRuta(1), null, null);
        when(tramoRepository.findById(10)).thenReturn(Optional.of(tramo));

        assertThrows(IllegalStateException.class, () -> tramoService.eliminar(10));
        verify(tramoRepository, never()).delete(tramo);
    }

    // --- Ciclo de Vida: Iniciar ---

    @Test
    void iniciar_success() {
        Ruta ruta = setupRuta(1);
        String subject = "kcid-transportista-1";
        Tramo tramo = setupTramo(20, EstadoTramo.ASIGNADO, ruta, "AAA111", subject);
        TramoLifecycleRequestDTO dto = new TramoLifecycleRequestDTO(LocalDateTime.now().plusHours(1), null);

        when(tramoRepository.findById(20)).thenReturn(Optional.of(tramo));

        tramoService.iniciar(20, dto, subject);

        assertEquals(EstadoTramo.INICIADO, tramo.getEstado());
        assertEquals(dto.getFechaHora(), tramo.getFechaHoraInicio());
        verify(tramoRepository).save(tramo);
        verify(rutaService).recalcularDesdeTramos(1);
    }

    @Test
    void iniciar_notAsignado_throws() {
        Tramo tramo = setupTramo(21, EstadoTramo.PLANIFICADO, setupRuta(1), "AAA111", "kcid");
        when(tramoRepository.findById(21)).thenReturn(Optional.of(tramo));

        assertThrows(IllegalStateException.class, () -> tramoService.iniciar(21, null, "kcid"));
    }

    @Test
    void iniciar_transportistaMismatch_throwsForbidden() {
        Tramo tramo = setupTramo(22, EstadoTramo.ASIGNADO, setupRuta(1), "AAA111", "kcid-real");
        when(tramoRepository.findById(22)).thenReturn(Optional.of(tramo));

        assertThrows(ResponseStatusException.class, () -> tramoService.iniciar(22, null, "kcid-otro"));
    }

    // --- Ciclo de Vida: Finalizar (Cálculo de Costo Real) ---

    @Test
    void finalizar_success_calculatesCost() {
        // Costo esperado (km=100): 10 (gestion) + (100km * 5) + (100km * 0.1 * 100) = 10 + 500 + 1000 = 1510.00
        BigDecimal kmRecorridos = BigDecimal.valueOf(100.0);
        Ruta ruta = setupRuta(2);
        Tramo tramo = setupTramo(30, EstadoTramo.INICIADO, ruta, "AAA111", "kcid-real");
        tramo.setDistancia(kmRecorridos); // Distancia del tramo

        Camion camion = setupCamion("AAA111", 5.0, 0.1f); // CostoBase 5.0, Consumo 0.1
        Tarifa tarifa = setupTarifaVigente();

        TramoLifecycleRequestDTO dto = new TramoLifecycleRequestDTO(LocalDateTime.now().plusHours(1), kmRecorridos);

        when(tramoRepository.findById(30)).thenReturn(Optional.of(tramo));
        when(camionService.findByDominio("AAA111")).thenReturn(camion);
        when(tarifaRepository.findByActivoTrue()).thenReturn(List.of(tarifa));

        TramoResponseDTO resp = tramoService.finalizar(30, dto, "kcid-real");

        assertEquals(EstadoTramo.FINALIZADO, tramo.getEstado());
        assertEquals(1510.00, tramo.getCostoReal().doubleValue());
        verify(tramoRepository).save(tramo);
    }

    @Test
    void finalizar_notIniciado_throws() {
        Tramo tramo = setupTramo(31, EstadoTramo.PLANIFICADO, setupRuta(1), "AAA111", "kcid");
        when(tramoRepository.findById(31)).thenReturn(Optional.of(tramo));
        assertThrows(IllegalStateException.class, () -> tramoService.finalizar(31, null, "kcid"));
    }

    // --- Asignar Camion ---

    @Test
    void asignarCamion_success_setsAsignado() {
        Ruta ruta = setupRuta(1);
        Tramo tramo = setupTramo(40, EstadoTramo.PLANIFICADO, ruta, null, null);
        Camion camion = setupCamion("AAA111", 1.0, 0.1f);

        when(tramoRepository.findById(40)).thenReturn(Optional.of(tramo));
        when(camionService.findByDominio("AAA111")).thenReturn(camion);

        tramoService.asignarCamion(40, new TramoAsignarCamionRequestDTO("AAA111"));

        assertEquals("AAA111", tramo.getDominioCamion());
        assertEquals(EstadoTramo.ASIGNADO, tramo.getEstado());
        verify(tramoRepository).save(tramo);
        verify(rutaService).recalcularDesdeTramos(1);
    }
}