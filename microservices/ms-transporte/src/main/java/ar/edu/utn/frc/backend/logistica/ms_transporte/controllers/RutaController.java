package ar.edu.utn.frc.backend.logistica.ms_transporte.controllers;

import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.ruta.RutaCreateRequestDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.ruta.RutaCreateResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.ruta.RutaResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.ruta.RutaUpdateRequestDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.RutaService;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Ruta;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rutas")
@RequiredArgsConstructor
public class RutaController {

    private final RutaService rutaService;

    @GetMapping
    public List<Ruta> getAll() {
        return rutaService.findAll();
    }

    @GetMapping("/{id}")
    public Ruta getById(@PathVariable Integer id) {
        return rutaService.findById(id);
    }

    @GetMapping("/solicitud/{solicitudId}")
    public Ruta getBySolicitud(@PathVariable Integer solicitudId) {
        return rutaService.findBySolicitud(solicitudId);
    }

    @PostMapping
    public RutaCreateResponseDTO create(@Valid @RequestBody RutaCreateRequestDTO dto) {
        return rutaService.crear(dto);
    }

    @PutMapping("/{id}")
    public RutaResponseDTO update(@PathVariable Integer id,
            @Valid @RequestBody RutaUpdateRequestDTO dto) {
        return rutaService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public RutaResponseDTO delete(@PathVariable Integer id) {
        return rutaService.eliminar(id);
    }
}
