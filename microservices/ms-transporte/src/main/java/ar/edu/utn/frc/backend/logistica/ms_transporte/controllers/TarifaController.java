package ar.edu.utn.frc.backend.logistica.ms_transporte.controllers;

import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tarifa.*;
import ar.edu.utn.frc.backend.logistica.ms_transporte.service.TarifaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tarifas")
public class TarifaController {

    @Autowired
    private TarifaService tarifaService;

    @GetMapping
    public List<TarifaListItemDTO> listarVigentes() {
        return tarifaService.listarVigentes();
    }

    @GetMapping("/{id}")
    public TarifaDetailDTO obtener(@PathVariable Integer id) {
        return tarifaService.obtenerPorId(id);
    }

    @PostMapping
    public TarifaCreateResponseDTO crear(@Valid @RequestBody TarifaCreateRequestDTO dto) {
        return tarifaService.crear(dto);
    }

    @PutMapping("/{id}")
    public TarifaResponseDTO actualizar(@PathVariable Integer id, @Valid @RequestBody TarifaUpdateRequestDTO dto) {
        return tarifaService.actualizar(id, dto);
    }
}

