package ar.edu.utn.frc.backend.logistica.ms_cliente.service;

import ar.edu.utn.frc.backend.logistica.ms_cliente.client.transporte.TransporteClient;
import ar.edu.utn.frc.backend.logistica.ms_cliente.client.transporte.dto.RutaCreateRequestDto;
import ar.edu.utn.frc.backend.logistica.ms_cliente.client.transporte.dto.RutaCreateResponseDto;
import ar.edu.utn.frc.backend.logistica.ms_cliente.client.transporte.dto.RutaDto;
import ar.edu.utn.frc.backend.logistica.ms_cliente.client.transporte.dto.TramoDto;
import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.solicitud.*;
import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.*;
import ar.edu.utn.frc.backend.logistica.ms_cliente.repository.ClienteRepository;
import ar.edu.utn.frc.backend.logistica.ms_cliente.repository.ContenedorRepository;
import ar.edu.utn.frc.backend.logistica.ms_cliente.repository.SolicitudRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceTest {

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ContenedorRepository contenedorRepository;

    @Mock
    private TransporteClient transporteClient; // Mock para la dependencia externa

    @InjectMocks
    private SolicitudService solicitudService;

    // --- Métodos de Setup ---
    private Cliente setupValidCliente(int id) {
        Cliente c = new Cliente(); c.setIdCliente(id); c.setKeycloakId("kcid"); c.setRazonSocial("Test RS"); c.setCuit("30"); c.setDireccionFacturacion("DF"); c.setDireccionEnvio("DE"); return c;
    }

    private Contenedor setupValidContenedor(int id, Cliente cliente) {
        Contenedor c = new Contenedor(); c.setIdContenedor(id); c.setIdentificacion("C-" + id); c.setCliente(cliente); c.setPeso(100.0); c.setVolumen(50.0); c.setEstado(ContenedorEstado.EN_ORIGEN); c.setActivo(true); return c;
    }

    private Solicitud setupValidSolicitud(int id, Cliente cliente, Contenedor contenedor) {
        Solicitud s = new Solicitud();
        s.setIdSolicitud(id);
        s.setCliente(cliente);
        s.setContenedor(contenedor);
        s.setEstado(SolicitudEstado.BORRADOR);
        s.setOrigenDireccion("Origen");
        s.setDestinoDireccion("Destino");
        s.setFechaCreacion(LocalDateTime.now());
        s.setFechaActualizacion(LocalDateTime.now());
        return s;
    }

    // --- Tests de Listado y Obtención ---

    @Test
    void listAll_returnsAllSolicitudes() {
        Cliente c1 = setupValidCliente(1);
        Contenedor con1 = setupValidContenedor(1, c1);
        Solicitud s1 = setupValidSolicitud(10, c1, con1);
        when(solicitudRepository.findAll()).thenReturn(List.of(s1));
        List<SolicitudListDTO> result = solicitudService.listAll();
        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getId());
    }

    @Test
    void getById_found_returnsDetails() {
        Cliente c1 = setupValidCliente(1);
        Contenedor con1 = setupValidContenedor(2, c1);
        Solicitud s = setupValidSolicitud(10, c1, con1);
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(s));

        SolicitudDetailsDTO result = solicitudService.getById(10);

        assertNotNull(result);
        assertEquals("Origen", result.getOrigenDireccion());
        assertEquals(1, result.getCliente().getId());
        assertEquals("C-2", result.getContenedorIdentificacion());
    }

    // --- Tests de Creación ---

    @Test
    void create_success() {
        Cliente cliente = setupValidCliente(1);
        Contenedor cont = setupValidContenedor(2, cliente);
        SolicitudCreateDTO dto = new SolicitudCreateDTO(1, 2, "O", "B", 1.0, 2.0, 2.0, 2.0);
        Solicitud saved = setupValidSolicitud(30, cliente, cont);

        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(contenedorRepository.findById(2)).thenReturn(Optional.of(cont));
        when(solicitudRepository.save(Mockito.isA(Solicitud.class))).thenReturn(saved);

        SolicitudResponseDTO resp = solicitudService.create(dto);
        assertEquals(30, resp.getId());
        verify(solicitudRepository).save(Mockito.isA(Solicitud.class));
    }

    @Test
    void create_contenedorDoesNotBelongToClient_throws() {
        Cliente c1 = setupValidCliente(1);
        Cliente c2 = setupValidCliente(99);
        Contenedor cont2 = setupValidContenedor(2, c2); // Contenedor pertenece a c2
        SolicitudCreateDTO dto = new SolicitudCreateDTO(1, 2, "O", "B", 1.0, 2.0, 2.0, 2.0); // Solicitud para c1

        when(clienteRepository.findById(1)).thenReturn(Optional.of(c1));
        when(contenedorRepository.findById(2)).thenReturn(Optional.of(cont2));

        assertThrows(IllegalStateException.class, () -> solicitudService.create(dto));
        verify(solicitudRepository, never()).save(Mockito.isA(Solicitud.class));
    }

    // --- Tests de Actualización de Estado (Lógica de Transporte) ---

    @Test
    void updateEstado_toProgramada_missingCoordinates_throws() {
        Solicitud s = setupValidSolicitud(40, setupValidCliente(1), setupValidContenedor(2, setupValidCliente(1)));
        s.setOrigenLatitud(null); // Faltan coordenadas
        SolicitudEstadoUpdateDTO dto = new SolicitudEstadoUpdateDTO(SolicitudEstado.PROGRAMADA, "Prueba");
        when(solicitudRepository.findById(40)).thenReturn(Optional.of(s));

        assertThrows(IllegalStateException.class, () -> solicitudService.updateEstado(40, dto));
        verify(transporteClient, never()).crearRuta(Mockito.isA(RutaCreateRequestDto.class));
    }

    @Test
    void updateEstado_toProgramada_success_calculatesCost() {

        Solicitud s = setupValidSolicitud(41, setupValidCliente(1), setupValidContenedor(2, setupValidCliente(1)));


        s.setOrigenLatitud(10.0);
        s.setOrigenLongitud(10.0);
        s.setDestinoLatitud(20.0);
        s.setDestinoLongitud(20.0);

        SolicitudEstadoUpdateDTO dto = new SolicitudEstadoUpdateDTO(SolicitudEstado.PROGRAMADA, "Prueba");


        final Integer ID_RUTA = 100;


        RutaCreateResponseDto createResp = new RutaCreateResponseDto(ID_RUTA, "Ruta creada");


        RutaDto rutaDto = new RutaDto();
        rutaDto.setIdRuta(ID_RUTA);
        rutaDto.setIdSolicitud(41);


        TramoDto tramo1 = new TramoDto();
        TramoDto tramo2 = new TramoDto();


        tramo1.setCostoAproximado(BigDecimal.valueOf(50.0));
        tramo2.setCostoAproximado(BigDecimal.valueOf(25.0));


        when(solicitudRepository.findById(41)).thenReturn(Optional.of(s));


        when(transporteClient.crearRuta(Mockito.isA(RutaCreateRequestDto.class))).thenReturn(createResp);

        when(transporteClient.obtenerRutaPorSolicitud(41)).thenReturn(rutaDto);


        when(transporteClient.obtenerTramosPorRuta(ID_RUTA)).thenReturn(List.of(tramo1, tramo2));


        when(solicitudRepository.save(Mockito.isA(Solicitud.class))).thenReturn(s);


        solicitudService.updateEstado(41, dto);


        verify(transporteClient).crearRuta(Mockito.isA(RutaCreateRequestDto.class));
        verify(transporteClient).obtenerRutaPorSolicitud(41);
        verify(transporteClient).obtenerTramosPorRuta(ID_RUTA);


        assertEquals(SolicitudEstado.PROGRAMADA, s.getEstado());


        assertEquals(75.0, s.getCostoEstimado());

        verify(solicitudRepository).save(s);
    }
}