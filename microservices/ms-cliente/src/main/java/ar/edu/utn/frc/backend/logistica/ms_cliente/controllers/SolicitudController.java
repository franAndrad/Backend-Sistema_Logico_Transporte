package ar.edu.utn.frc.backend.logistica.ms_cliente.controllers;

import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.solicitud.*;
import ar.edu.utn.frc.backend.logistica.ms_cliente.service.SolicitudService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @GetMapping
    public ResponseEntity<List<SolicitudListDTO>> listAll() {
        return ResponseEntity.ok(solicitudService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudDetailsDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(solicitudService.getById(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<SolicitudListDTO>> listByCliente(@PathVariable Integer clienteId) {
        return ResponseEntity.ok(solicitudService.listByCliente(clienteId));
    }

    @GetMapping("/{id}/estado")
    public ResponseEntity<SolicitudEstadoDTO> getEstado(@PathVariable Integer id) {
        return ResponseEntity.ok(solicitudService.getEstado(id));
    }

    @PostMapping
    public ResponseEntity<SolicitudResponseDTO> create(@Valid @RequestBody SolicitudCreateDTO dto) {
        SolicitudResponseDTO response = solicitudService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SolicitudResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody SolicitudUpdateDTO dto) {
        return ResponseEntity.ok(solicitudService.update(id, dto));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<SolicitudResponseDTO> updateEstado(@PathVariable Integer id, @Valid @RequestBody SolicitudEstadoUpdateDTO dto) {
        return ResponseEntity.ok(solicitudService.updateEstado(id, dto));
    }
}

