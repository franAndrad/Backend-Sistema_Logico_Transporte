package ar.edu.utn.frc.backend.logistica.ms_transporte.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class TransporteController {

    @GetMapping("/api/v1/rutas/health")
    public String healthCheck() {
        return "ms-transporte OK!";
    }
}
