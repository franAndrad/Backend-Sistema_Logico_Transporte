package ar.edu.utn.frc.backend.logistica.ms_transporte.service.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.ruta.*;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Camion;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Ruta;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Tarifa;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Tramo;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.enums.EstadoRuta;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.enums.EstadoTramo;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.enums.TipoTramo;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.RutaRepository;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.TarifaRepository;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.TramoRepository;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.CamionService;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.GoogleMapsService;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.RutaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils; // Para inyectar mocks/spies en métodos privados

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RutaServiceTest {

    @Mock private RutaRepository rutaRepository;
    @Mock private TramoRepository tramoRepository;
    @Mock private GoogleMapsService googleMapsService;
    @Mock private TarifaRepository tarifaRepository;
    @Mock private CamionService camionService;

    // Usamos @Spy para simular llamadas a métodos privados/dependencias simuladas
    @InjectMocks @Spy
    private RutaService rutaService;

    private Ruta setupRuta(int id, EstadoRuta estado) {
        Ruta r = new Ruta();
        r.setIdRuta(id);
        r.setIdSolicitud(100 + id);
        r.setEstado(estado);
        return r;
    }

    private Tarifa setupTarifaVigente() {
        Tarifa t = new Tarifa();
        t.setValorPorTramo(10.0f);
        t.setValorPorKm(0.5f);
        t.setValorLitroCombustible(100.0f);
        return t;
    }

    private Camion setupCamion(double consumo) {
        Camion c = new Camion();
        c.setDominio("TEST");
        c.setConsumoCombustible(2.00f);
        return c;
    }



    @Test
    void crear_success_createsRutaAndTramosWithCalculatedCost() {
        RutaCreateRequestDTO dto = new RutaCreateRequestDTO(101, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ONE, List.of(1));
        Ruta savedRuta = setupRuta(1, EstadoRuta.ESTIMADA);

        // Mocks de Google Maps
        LegCalculadoDTO leg1 = new LegCalculadoDTO(BigDecimal.valueOf(10.0), 30L, null, 1, "ORIGEN_DEPOSITO");
        LegCalculadoDTO leg2 = new LegCalculadoDTO(BigDecimal.valueOf(5.0), 15L, 1, null, "DEPOSITO_DESTINO");
        RutaCalculadaDTO calculada = new RutaCalculadaDTO(BigDecimal.valueOf(15.0), 45L, List.of(leg1, leg2));

        // Mocks de Tarifa y Consumo
        when(tarifaRepository.findByActivoTrue()).thenReturn(Optional.of(setupTarifaVigente()).stream().toList());
        when(camionService.findAllDisponibles()).thenReturn(List.of(setupCamion(0.1))); // Consumo promedio 0.1

        // Simulación de llamadas
        when(googleMapsService.calcularRutaYTramos(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyList())).thenReturn(calculada);
        when(rutaRepository.save(Mockito.isA(Ruta.class))).thenReturn(savedRuta);

        // Necesitamos simular el método privado calcularCostoAproximadoTramo
        // 10km * 0.5 + 10(gestión) + 10km * 0.1 * 100 = 5 + 10 + 100 = 115
        // 5km * 0.5 + 10(gestión) + 5km * 0.1 * 100 = 2.5 + 10 + 50 = 62.5

        // El test real debería depender del método privado, pero para la prueba unitaria:
        // Llamamos al método real de calcularCostoAproximadoTramo (ya que RutaService es @Spy)

        RutaCreateResponseDTO resp = rutaService.crear(dto);

        assertEquals(1, resp.getIdRuta());
        verify(rutaRepository).save(Mockito.isA(Ruta.class));
        verify(tramoRepository, times(2)).save(Mockito.isA(Tramo.class));

        // Verificación de que el costo aproximado se calculó y se guardó en los tramos
        verify(tramoRepository, times(2)).save(Mockito.argThat(t -> {
            boolean isCost1 = t.getCostoAproximado().compareTo(BigDecimal.valueOf(115.00)) == 0;
            boolean isCost2 = t.getCostoAproximado().compareTo(BigDecimal.valueOf(62.50)) == 0;
            return isCost1 || isCost2;
        }));
    }

    // --- Recalcular desde Tramos (Lógica de Estado de Ruta) ---

    @Test
    void recalcularDesdeTramos_updatesStateToCompletada() {
        Ruta ruta = setupRuta(1, EstadoRuta.ASIGNADA);
        Tramo t1 = new Tramo(); t1.setEstado(EstadoTramo.FINALIZADO); t1.setDistancia(BigDecimal.valueOf(100));
        Tramo t2 = new Tramo(); t2.setEstado(EstadoTramo.FINALIZADO); t2.setDistancia(BigDecimal.valueOf(50));

        when(rutaRepository.findById(1)).thenReturn(Optional.of(ruta));
        when(tramoRepository.findByRuta(ruta)).thenReturn(List.of(t1, t2));

        rutaService.recalcularDesdeTramos(1);

        assertEquals(EstadoRuta.COMPLETADA, ruta.getEstado());
        assertEquals(BigDecimal.valueOf(150), ruta.getDistanciaTotal());
        assertEquals(2, ruta.getCantidadTramos());
        verify(rutaRepository).save(ruta);
    }

    @Test
    void recalcularDesdeTramos_updatesStateToEnProgreso() {
        Ruta ruta = setupRuta(2, EstadoRuta.ASIGNADA);
        Tramo t1 = new Tramo(); t1.setEstado(EstadoTramo.FINALIZADO);
        Tramo t2 = new Tramo(); t2.setEstado(EstadoTramo.INICIADO);
        Tramo t3 = new Tramo(); t3.setEstado(EstadoTramo.PLANIFICADO);

        when(rutaRepository.findById(2)).thenReturn(Optional.of(ruta));
        when(tramoRepository.findByRuta(ruta)).thenReturn(List.of(t1, t2, t3));

        rutaService.recalcularDesdeTramos(2);

        assertEquals(EstadoRuta.EN_PROGRESO, ruta.getEstado());
    }

    // --- Eliminación ---

    @Test
    void eliminar_success_noStartedTramo() {
        Ruta ruta = setupRuta(3, EstadoRuta.ESTIMADA);
        Tramo t1 = new Tramo(); t1.setEstado(EstadoTramo.PLANIFICADO);

        when(rutaRepository.findById(3)).thenReturn(Optional.of(ruta));
        when(tramoRepository.findByRuta(ruta)).thenReturn(List.of(t1));

        rutaService.eliminar(3);

        verify(tramoRepository).deleteAll(List.of(t1));
        verify(rutaRepository).delete(ruta);
    }

    @Test
    void eliminar_tranosIniciados_throws() {
        Ruta ruta = setupRuta(4, EstadoRuta.EN_PROGRESO);
        Tramo t1 = new Tramo(); t1.setEstado(EstadoTramo.INICIADO);

        when(rutaRepository.findById(4)).thenReturn(Optional.of(ruta));
        when(tramoRepository.findByRuta(ruta)).thenReturn(List.of(t1));

        assertThrows(IllegalStateException.class, () -> rutaService.eliminar(4));

        verify(tramoRepository, never()).deleteAll(any());
        verify(rutaRepository, never()).delete(any());
    }
}