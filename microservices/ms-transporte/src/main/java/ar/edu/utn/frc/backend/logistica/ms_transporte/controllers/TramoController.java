package ar.edu.utn.frc.backend.logistica.ms_transporte.controllers;

import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tramo.TramoAsignarCamionRequestDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tramo.TramoLifecycleRequestDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tramo.TramoCreateResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tramo.TramoUpdateRequestDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tramo.TramoCreateRequestDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tramo.TramoResponseDTO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.TramoService;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Tramo;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tramos")
@RequiredArgsConstructor
public class TramoController {

    private final TramoService tramoService;

    @GetMapping
    public List<Tramo> getAll() {
        return tramoService.findAll();
    }

    @GetMapping("/{id}")
    public Tramo getById(@PathVariable Integer id) {
        return tramoService.findById(id);
    }

    @GetMapping("/ruta/{rutaId}")
    public List<Tramo> getByRuta(@PathVariable Integer rutaId) {
        return tramoService.findByRuta(rutaId);
    }

    @PostMapping
    public TramoCreateResponseDTO create(@Valid @RequestBody TramoCreateRequestDTO dto) {
        return tramoService.crear(dto);
    }

    @PutMapping("/{id}")
    public TramoResponseDTO update(@PathVariable Integer id,
            @Valid @RequestBody TramoUpdateRequestDTO dto) {
        return tramoService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public TramoResponseDTO delete(@PathVariable Integer id) {
        return tramoService.eliminar(id);
    }

    @PostMapping("/{id}/iniciar")
    public TramoResponseDTO iniciar(@PathVariable Integer id,
            @RequestBody(required = false) TramoLifecycleRequestDTO body,
            @AuthenticationPrincipal Jwt jwt) {
        if (body == null)
            body = new TramoLifecycleRequestDTO();
        String subject = jwt != null ? jwt.getSubject() : null;
        return tramoService.iniciar(id, body, subject);
    }

    @PostMapping("/{id}/finalizar")
    public TramoResponseDTO finalizar(@PathVariable Integer id,
            @RequestBody TramoLifecycleRequestDTO body,
            @AuthenticationPrincipal Jwt jwt) {
        String subject = jwt != null ? jwt.getSubject() : null;
        return tramoService.finalizar(id, body, subject);
    }

    @PutMapping("/{id}/camion")
    public TramoResponseDTO asignarCamion(
            @PathVariable Integer id,
            @Valid @RequestBody TramoAsignarCamionRequestDTO dto) {
        return tramoService.asignarCamion(id, dto);
    }

    @DeleteMapping("/{id}/camion")
    public TramoResponseDTO desasignarCamion(@PathVariable Integer id) {
        return tramoService.desasignarCamion(id);
    }
}
