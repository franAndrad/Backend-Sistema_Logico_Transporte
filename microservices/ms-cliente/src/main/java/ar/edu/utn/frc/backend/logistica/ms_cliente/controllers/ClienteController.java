package ar.edu.utn.frc.backend.logistica.ms_cliente.controllers;

import ar.edu.utn.frc.backend.logistica.ms_cliente.service.ClienteService;
import ar.edu.utn.frc.backend.logistica.ms_cliente.dto.cliente.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    @GetMapping("/health")
    public String healthCheck() {
        return "ms-clientes OK!";
    }

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping

    public ResponseEntity<List<ClienteListDTO>> listAll() {
        return ResponseEntity.ok(clienteService.listAll());
    }

    @GetMapping("/{id}")

    public ResponseEntity<ClienteDetailsDTO> getById(
            @PathVariable Integer id,
            Authentication auth) {
        return ResponseEntity.ok(clienteService.getById(id, auth));
    }

    @PostMapping

    public ResponseEntity<ClienteResponseDTO> create(
            @Valid @RequestBody ClienteCreateDTO dto) {
        ClienteResponseDTO response = clienteService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")

    public ResponseEntity<ClienteResponseDTO> update(
            @PathVariable Integer id,
            @Valid @RequestBody ClienteUpdateDTO dto) {
        return ResponseEntity.ok(clienteService.update(id, dto, null));
    }

    @DeleteMapping("/{id}")

    public ResponseEntity<ClienteResponseDTO> delete(@PathVariable Integer id) {
        return ResponseEntity.ok(clienteService.delete(id));
    }
}