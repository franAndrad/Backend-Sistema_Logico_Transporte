package ar.edu.utn.frc.backend.logistica.ms_transporte.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.camion.CamionCreateRequestDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.camion.CamionCreateResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.camion.CamionResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.camion.CamionUpdateResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Camion;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.CamionService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/camiones")
public class CamionController {

    @Autowired
    private CamionService camionService;

    @GetMapping
    public List<Camion> getAll() {
        return camionService.findAll();
    }

    @GetMapping("/{dominio}")
    public Camion getByDominio(@PathVariable String dominio) {
        return camionService.findByDominio(dominio);
    }

    @GetMapping("/disponibles")
    public List<Camion> getAllDisponibles() {
        return camionService.findAllDisponibles();
    }

    @PostMapping
    public CamionCreateResponseDTO create(@Valid @RequestBody CamionCreateRequestDTO dto) {
        Camion creado = camionService.save(dto);
        return new CamionCreateResponseDTO(creado.getDominio(), "Camión registrado");
    }

    @PutMapping("/{dominio}")
    public CamionCreateResponseDTO actualizar(
            @PathVariable String dominio,
            @Valid @RequestBody CamionUpdateResponseDTO dto) {
        return camionService.actualizarCamion(dominio, dto);
    }

    @PatchMapping("/{dominio}/habilitar")
    public CamionResponseDTO habilitar(@PathVariable String dominio) {
        return camionService.habilitar(dominio);
    }

    @PatchMapping("/{dominio}/deshabilitar")
    public CamionResponseDTO deshabilitar(@PathVariable String dominio) {
        return camionService.deshabilitar(dominio);
    }
}
