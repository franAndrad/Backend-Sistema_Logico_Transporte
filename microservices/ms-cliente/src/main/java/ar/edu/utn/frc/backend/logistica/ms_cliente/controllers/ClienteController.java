package ar.edu.utn.frc.backend.logistica.ms_cliente.controllers;

import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.*;
import ar.edu.utn.frc.backend.logistica.ms_cliente.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    //@PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<List<ClienteListDTO>> listAll() {
        return ResponseEntity.ok(clienteService.listAll());
    }

    @GetMapping("/{id}")
    //@PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR', 'ADMIN')")
    public ResponseEntity<ClienteDetailsDTO> getById(
            @PathVariable Integer id,
            Authentication auth) {
        return ResponseEntity.ok(clienteService.getById(id, auth));
    }

    @PostMapping
    //@PreAuthorize("permitAll()")
    public ResponseEntity<ClienteResponseDTO> create(
            @Valid @RequestBody ClienteCreateDTO dto) {
        ClienteResponseDTO response = clienteService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    //@PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR')")
    public ResponseEntity<ClienteResponseDTO> update(
            @PathVariable Integer id,
            @Valid @RequestBody ClienteUpdateDTO dto) {
        return ResponseEntity.ok(clienteService.update(id, dto, null));
    }

    @DeleteMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponseDTO> delete(@PathVariable Integer id) {
        return ResponseEntity.ok(clienteService.delete(id));
    }
}