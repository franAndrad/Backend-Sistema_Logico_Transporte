package ar.edu.utn.frc.backend.logistica.ms_transporte.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1/transportes")
public class TransporteController {

    @GetMapping("/health")
    public String healthCheck() {
        return "ms-transporte OK!";
    }
}
