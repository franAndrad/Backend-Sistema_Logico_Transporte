package ar.edu.utn.frc.backend.logistica.ms_transporte.controllers;

import ar.edu.utn.frc.backend.logistica.ms_transporte.domain.RutaSeleccionada;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.DistanciaResponse;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.RutaSeleccionadaRepository;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.DistanciaService;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.dto.RutasResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/distancias")
@RequiredArgsConstructor
public class DistanciaController {
    
    private final DistanciaService distanciaService;
    private final RutaSeleccionadaRepository rutaRepository;
    
    /**
     * PASO 1: Cliente consulta todas las rutas disponibles
     * Esto se CACHEA en memoria por 15 minutos
     * GET /api/v1/distancias/rutas-alternativas?origenLat=-34.603722&origenLng=-58.381592&destinoLat=-34.921230&destinoLng=-57.954590
     */
    @GetMapping("/rutas-alternativas")
    public ResponseEntity<RutasResponse> obtenerRutasAlternativas(
        @RequestParam Double origenLat,
        @RequestParam Double origenLng,
        @RequestParam Double destinoLat,
        @RequestParam Double destinoLng
    ) {
        System.out.println("🚗 PASO 1: Consultando rutas alternativas");
        System.out.println("📍 Origen: " + origenLat + "," + origenLng);
        System.out.println("📍 Destino: " + destinoLat + "," + destinoLng);
        
        RutasResponse rutas = distanciaService.obtenerTodasLasRutas(
            origenLat, origenLng, destinoLat, destinoLng
        );
        
        System.out.println("✅ " + rutas.getTotalRutas() + " rutas encontradas");
        System.out.println("💾 Guardadas en CACHÉ (próxima consulta será instantánea)");
        
        return ResponseEntity.ok(rutas);
    }
    
    /**
     * PASO 2: Cliente selecciona UNA ruta y se GUARDA EN BD
     * Esto reutiliza el caché (no llama de nuevo a Google Maps)
     * POST /api/v1/distancias/seleccionar-ruta
     */
    @PostMapping("/seleccionar-ruta")
    public ResponseEntity<RutaSeleccionada> seleccionarYGuardarRuta(
        @RequestParam Double origenLat,
        @RequestParam Double origenLng,
        @RequestParam Double destinoLat,
        @RequestParam Double destinoLng,
        @RequestParam Integer numeroRuta,
        @RequestParam(required = false) Long clienteId,
        @RequestParam(required = false) Long viajeId
    ) {
        System.out.println("💾 PASO 2: Cliente selecciona ruta #" + numeroRuta);
        System.out.println("👤 Cliente ID: " + clienteId);
        System.out.println("🚚 Viaje ID: " + viajeId);
        
        // Obtener rutas del CACHÉ (no llama a Google Maps)
        RutasResponse rutasResponse = distanciaService.obtenerTodasLasRutas(
            origenLat, origenLng, destinoLat, destinoLng
        );
        
        System.out.println("⚡ Datos obtenidos del CACHÉ (sin llamar a Google Maps)");
        
        // Buscar la ruta seleccionada
        var rutaSeleccionada = rutasResponse.getTodasLasRutas().stream()
            .filter(r -> r.getNumeroRuta().equals(numeroRuta))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(
                "Ruta #" + numeroRuta + " no encontrada. Rutas disponibles: 1-" + 
                rutasResponse.getTotalRutas()
            ));
        
        // Crear entidad para persistir en BD
        RutaSeleccionada entidad = new RutaSeleccionada();
        entidad.setClienteId(clienteId);
        entidad.setViajeId(viajeId);
        entidad.setOrigenLat(origenLat);
        entidad.setOrigenLng(origenLng);
        entidad.setDestinoLat(destinoLat);
        entidad.setDestinoLng(destinoLng);
        entidad.setNumeroRuta(rutaSeleccionada.getNumeroRuta());
        entidad.setDistanciaKm(rutaSeleccionada.getDistanciaKm());
        entidad.setDuracionMinutos(rutaSeleccionada.getDuracionMinutos());
        entidad.setResumen(rutaSeleccionada.getResumen());
        entidad.setEsMasRapida(rutaSeleccionada.getEsMasRapida());
        
        // Guardar en BD
        RutaSeleccionada guardada = rutaRepository.save(entidad);
        
        System.out.println("✅ Ruta guardada en BD con ID: " + guardada.getId());
        System.out.println("📊 Distancia: " + guardada.getDistanciaKm() + " km");
        System.out.println("⏱️  Duración: " + guardada.getDuracionMinutos() + " min");
        
        return ResponseEntity.ok(guardada);
    }
    
    /**
     * PASO 3: Obtener una ruta guardada por su ID
     * GET /api/v1/distancias/ruta/{id}
     */
    @GetMapping("/ruta/{id}")
    public ResponseEntity<RutaSeleccionada> obtenerRutaPorId(@PathVariable Long id) {
        System.out.println("🔍 Buscando ruta con ID: " + id);
        
        return rutaRepository.findById(id)
            .map(ruta -> {
                System.out.println("✅ Ruta encontrada en BD");
                System.out.println("📊 Cliente ID: " + ruta.getClienteId());
                System.out.println("🚚 Viaje ID: " + ruta.getViajeId());
                System.out.println("📍 Distancia: " + ruta.getDistanciaKm() + " km");
                System.out.println("⏱️  Duración: " + ruta.getDuracionMinutos() + " min");
                return ResponseEntity.ok(ruta);
            })
            .orElseGet(() -> {
                System.out.println("❌ Ruta con ID " + id + " no encontrada");
                return ResponseEntity.notFound().build();
            });
    }
    
    /**
     * PASO 4: Obtener historial de rutas por cliente
     * GET /api/v1/distancias/historial?clienteId=123
     */
    @GetMapping("/historial")
    public ResponseEntity<?> obtenerHistorialCliente(@RequestParam Long clienteId) {
        System.out.println("📜 Consultando historial del cliente ID: " + clienteId);
        
        var rutas = rutaRepository.findByClienteId(clienteId);
        
        System.out.println("✅ Encontradas " + rutas.size() + " rutas para el cliente");
        
        return ResponseEntity.ok(rutas);
    }
    
}