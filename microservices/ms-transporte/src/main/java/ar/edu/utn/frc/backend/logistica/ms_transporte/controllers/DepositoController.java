package ar.edu.utn.frc.backend.logistica.ms_transporte.controllers;

import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Deposito;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.DepositoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/depositos")
public class DepositoController {

    @Autowired
    private DepositoService depositoService;

    @GetMapping
    public List<Deposito> getAll() {
        return depositoService.findAll();
    }

    @GetMapping("/{idDeposito}")
    public Deposito getById(@PathVariable Integer idDeposito) {
        return depositoService.findById(idDeposito);
    }

    @PostMapping
    public Deposito create(@Valid @RequestBody Deposito deposito) {
        return depositoService.save(deposito);
    }
}
