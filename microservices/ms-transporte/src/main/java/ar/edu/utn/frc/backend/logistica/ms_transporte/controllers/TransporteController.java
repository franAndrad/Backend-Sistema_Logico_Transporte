package ar.edu.utn.frc.backend.logistica.ms_transporte.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/rutas")
@RestController
public class TransporteController {

    @GetMapping("/health")
    public String healthCheck() {
        return "ms-transporte OK!";
    }
}
