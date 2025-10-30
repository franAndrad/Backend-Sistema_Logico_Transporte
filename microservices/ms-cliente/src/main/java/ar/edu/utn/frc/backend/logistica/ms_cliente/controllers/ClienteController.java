package ar.edu.utn.frc.backend.logistica.ms_cliente.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/clientes")
@RestController
public class ClienteController {
    
    @GetMapping("/health")
    public String healthCheck() {
        return "ms-clientes OK!";
    }
}
