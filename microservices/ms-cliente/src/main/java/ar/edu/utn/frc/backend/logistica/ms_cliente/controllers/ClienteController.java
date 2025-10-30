package ar.edu.utn.frc.backend.logistica.ms_cliente.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class ClienteController {
    
    @GetMapping("/api/v1/clientes/health")
    public String healthCheck() {
        return "ms-clientes OK!";
    }
}
