package ar.edu.utn.frc.backend.logistica.ms_transporte.controllers;

import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.deposito.*;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Deposito;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.DepositoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/depositos")
public class DepositoController {

    @Autowired
    private DepositoService depositoService;

    @GetMapping
    public List<Deposito> getAll() {
        return depositoService.findAll();
    }

    @GetMapping("/activos")
    public List<Deposito> getAllActives() {
        return depositoService.findAllActivos();
    }

    @GetMapping("/{idDeposito}")
    public Deposito getById(@PathVariable Integer idDeposito) {
        return depositoService.findById(idDeposito);
    }

    @GetMapping("/cercanos")
    public List<DepositoCercanoResponseDTO> buscarCercanos(
            @RequestParam Double latitud,
            @RequestParam Double longitud
    ) {
        return depositoService.buscarDepositosCercanos(latitud, longitud);
    }

    @PostMapping
    public DepositoCreateResponseDTO create(
            @Valid @RequestBody DepositoCreateRequestDTO dto) {
        return depositoService.save(dto);
    }

    @PutMapping("/{idDeposito}")
    public DepositoResponseDTO actualizar(
            @PathVariable Integer idDeposito,
            @Valid @RequestBody DepositoUpdateResponseDTO dto) {
        return depositoService.actualizarDeposito(idDeposito, dto);
    }

    @PatchMapping("/{idDeposito}/desactivar")
    public DepositoResponseDTO desactivar(@PathVariable Integer idDeposito) {
        return depositoService.desactivar(idDeposito);
    }

    @PatchMapping("/{idDeposito}/activar")
    public DepositoResponseDTO activar(@PathVariable Integer idDeposito) {
        return depositoService.activar(idDeposito);
    }

}