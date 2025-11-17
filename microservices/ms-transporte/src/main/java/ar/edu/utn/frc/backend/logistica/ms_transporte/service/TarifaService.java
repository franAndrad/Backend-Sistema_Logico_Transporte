package ar.edu.utn.frc.backend.logistica.ms_transporte.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.tarifa.*;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Tarifa;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.TarifaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TarifaService {

    private final TarifaRepository tarifaRepository;

    public List<TarifaListItemDTO> listarVigentes() {
        log.info("Listando tarifas vigentes");

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
        log.info("Buscando tarifa con id {}", id);

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
        log.info("Creando nueva tarifa: {}", dto.getConcepto());

        // Desactivar cualquier tarifa activa previa
        if (tarifaRepository.existsByActivoTrue()) {
            tarifaRepository.findByActivoTrue().forEach(t -> {
                t.setActivo(false);
                tarifaRepository.save(t);
            });
        }

        Tarifa nueva = new Tarifa();
        nueva.setConcepto(dto.getConcepto());
        nueva.setValorBase(dto.getValorBase());
        nueva.setValorPorKm(dto.getValorPorKm());
        nueva.setValorPorPeso(dto.getValorPorPeso());
        nueva.setValorPorVolumen(dto.getValorPorVolumen());
        nueva.setValorPorTramo(dto.getValorPorTramo());
        nueva.setValorLitroCombustible(dto.getValorLitroCombustible());
        nueva.setFechaVigencia(dto.getFechaVigencia());
        nueva.setActivo(true);

        Tarifa guardada = tarifaRepository.save(nueva);

        return new TarifaCreateResponseDTO(
                guardada.getIdTarifa(),
                "Tarifa creada correctamente"
        );
    }

    public TarifaResponseDTO actualizar(Integer id, TarifaUpdateRequestDTO dto) {
        log.info("Actualizando tarifa {}", id);

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
            // Activar esta y desactivar otras
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

        return new TarifaResponseDTO(
                id,
                "Tarifa actualizada correctamente"
        );
    }
}
