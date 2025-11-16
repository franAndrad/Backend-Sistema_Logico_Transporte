package ar.edu.utn.frc.backend.logistica.ms_transporte.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tarifa.*;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Tarifa;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.TarifaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class TarifaService {

    @Autowired
    private TarifaRepository tarifaRepository;

    // Lista solo vigentes (activas)
    public List<TarifaListItemDTO> listarVigentes() {
        return tarifaRepository.findByActivoTrue().stream()
                .map(t -> new TarifaListItemDTO(
                        t.getIdTarifa(),
                        t.getConcepto(),
                        t.getValorBase(),
                        t.getValorPorKm(),
                        t.getValorPorPeso(),
                        t.getValorPorVolumen(),
                        t.getFechaVigencia(),
                        t.getActivo()
                ))
                .collect(Collectors.toList());
    }

    public TarifaDetailDTO obtenerPorId(Integer id) {
        Tarifa t = tarifaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tarifa no encontrada"));
        return new TarifaDetailDTO(
                t.getIdTarifa(),
                t.getConcepto(),
                t.getValorBase(),
                t.getValorPorKm(),
                t.getValorPorPeso(),
                t.getValorPorVolumen(),
                t.getValorPorTramo(),
                t.getValorLitroCombustible(),
                t.getFechaVigencia(),
                t.getActivo()
        );
    }

    public TarifaCreateResponseDTO crear(TarifaCreateRequestDTO dto) {
        // Si hay una activa y la nueva también será activa, desactivamos la anterior.
        if (tarifaRepository.existsByActivoTrue()) {
            // desactivar todas (solo debería haber una) para asegurar unicidad
            tarifaRepository.findByActivoTrue().forEach(t -> {
                t.setActivo(false);
                tarifaRepository.save(t);
            });
        }
        Tarifa t = new Tarifa();
        t.setConcepto(dto.getConcepto());
        t.setValorBase(dto.getValorBase());
        t.setValorPorKm(dto.getValorPorKm());
        t.setValorPorPeso(dto.getValorPorPeso());
        t.setValorPorVolumen(dto.getValorPorVolumen());
        t.setValorPorTramo(dto.getValorPorTramo());
        t.setValorLitroCombustible(dto.getValorLitroCombustible());
        t.setFechaVigencia(dto.getFechaVigencia());
        t.setActivo(true); // nueva tarifa vigente
        Tarifa guardada = tarifaRepository.save(t);
        return new TarifaCreateResponseDTO(guardada.getIdTarifa(), "Tarifa creada correctamente");
    }

    public TarifaResponseDTO actualizar(Integer id, TarifaUpdateRequestDTO dto) {
        Tarifa t = tarifaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tarifa no encontrada"));

        t.setValorBase(dto.getValorBase());
        t.setValorPorKm(dto.getValorPorKm());
        t.setValorPorPeso(dto.getValorPorPeso());
        t.setValorPorVolumen(dto.getValorPorVolumen());
        t.setValorPorTramo(dto.getValorPorTramo());
        t.setValorLitroCombustible(dto.getValorLitroCombustible());
        t.setFechaVigencia(dto.getFechaVigencia());

        if (dto.getActivo()) {
            // activar esta y desactivar otras
            tarifaRepository.findByActivoTrue().forEach(other -> {
                if (!other.getIdTarifa().equals(t.getIdTarifa())) {
                    other.setActivo(false);
                    tarifaRepository.save(other);
                }
            });
            t.setActivo(true);
        } else {
            t.setActivo(false);
        }

        tarifaRepository.save(t);
        return new TarifaResponseDTO(id, "Tarifa actualizada correctamente");
    }
}

