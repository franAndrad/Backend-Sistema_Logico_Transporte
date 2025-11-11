package ar.edu.utn.frc.backend.logistica.ms_cliente.controllers;

import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.contenedor.*;
import ar.edu.utn.frc.backend.logistica.ms_cliente.service.ContenedorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contenedores")
public class ContenedorController {

    private final ContenedorService contenedorService;

    public ContenedorController(ContenedorService contenedorService) {
        this.contenedorService = contenedorService;
    }

    @GetMapping
    public ResponseEntity<List<ContenedorListDTO>> listAll() {
        return ResponseEntity.ok(contenedorService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContenedorDetailsDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(contenedorService.getById(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ContenedorListDTO>> listByCliente(@PathVariable Integer clienteId) {
        return ResponseEntity.ok(contenedorService.listByCliente(clienteId));
    }

    @PostMapping
    public ResponseEntity<ContenedorResponseDTO> create(@Valid @RequestBody ContenedorCreateDTO dto) {
        ContenedorResponseDTO response = contenedorService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContenedorResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody ContenedorUpdateDTO dto) {
        return ResponseEntity.ok(contenedorService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ContenedorResponseDTO> delete(@PathVariable Integer id) {
        return ResponseEntity.ok(contenedorService.delete(id));
    }
}

