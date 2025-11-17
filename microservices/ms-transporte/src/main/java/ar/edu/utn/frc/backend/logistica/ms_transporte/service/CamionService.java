package ar.edu.utn.frc.backend.logistica.ms_transporte.service;

import java.util.List;
import java.util.NoSuchElementException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.camion.CamionCreateRequestDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.camion.CamionCreateResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.camion.CamionResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.camion.CamionUpdateRequestDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Camion;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.CamionRepository;
import lombok.NonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class CamionService {
    
    @Autowired
    private CamionRepository camionRepository;

    public List<Camion> findAll() {
        log.info("Obteniendo todos los camiones");
        List<Camion> lista = camionRepository.findAll();
        log.debug("Cantidad de camiones encontrados: {}", lista.size());
        return lista;
    }

    public List<Camion> findAllDisponibles() {
        log.info("Obteniendo camiones disponibles");
        List<Camion> lista = camionRepository.findByDisponibilidadTrue();
        log.debug("Camiones disponibles encontrados: {}", lista.size());
        return lista;
    }

    public Camion findByDominio(@NonNull String dominio) {
        log.info("Buscando camión con dominio {}", dominio);

        return camionRepository.findById(dominio)
                .orElseThrow(() -> new IllegalStateException("Camión no encontrado"));
    }

    public Camion save(CamionCreateRequestDTO dto) {
        log.info("Intentando registrar camión con dominio {}", dto.getDominio());

        if (camionRepository.existsByDominio(dto.getDominio())) {
            log.warn("Intento fallido: ya existe un camión con dominio {}", dto.getDominio());
            throw new IllegalStateException("Ya existe un camión con ese dominio");
        }

        Camion camion = new Camion();
        camion.setDominio(dto.getDominio());
        camion.setCapacidad(dto.getCapacidad());
        camion.setVolumen(dto.getVolumen());
        camion.setDisponibilidad(true);
        camion.setCostoBase(dto.getCostoBase());
        camion.setConsumoCombustible(dto.getConsumoCombustible());

        camionRepository.save(camion);

        log.info("Camión registrado correctamente: {}", camion.getDominio());
        return camion;
    }

    public CamionCreateResponseDTO actualizarCamion(@NonNull String dominio, CamionUpdateRequestDTO dto) {
        log.info("Actualizando camión con dominio {}", dominio);

        Camion camion = camionRepository.findById(dominio)
                .orElseThrow(() -> new NoSuchElementException("Camión no encontrado"));

        if (!camion.getDisponibilidad()) {
            log.warn("No se puede actualizar camión {}: no está disponible", dominio);
            throw new IllegalStateException("No se puede actualizar un camión no disponible");
        }

        camion.setCapacidad(dto.getCapacidad());
        camion.setVolumen(dto.getVolumen());
        camion.setCostoBase(dto.getCostoBase());
        camion.setConsumoCombustible(dto.getConsumoCombustible());

        camionRepository.save(camion);

        log.info("Camión {} actualizado correctamente", dominio);

        return new CamionCreateResponseDTO(dominio, "Camión actualizado con éxito");
    }

    public CamionResponseDTO habilitar(@NonNull String dominio) {
        log.info("Habilitando camión con dominio {}", dominio);

        Camion camion = camionRepository.findById(dominio)
                .orElseThrow(() -> new NoSuchElementException("Camión no encontrado"));

        if (!camion.getDisponibilidad()) {
            log.debug("El camión {} ya estaba habilitado", dominio);
            return new CamionResponseDTO(dominio, "El camión ya estaba habilitado");
        }

        camion.setDisponibilidad(true);
        camionRepository.save(camion);

        log.info("Camión {} habilitado correctamente", dominio);

        return new CamionResponseDTO(dominio, "Camión habilitado correctamente");
    }

    // Falta Tramos para validar que no este en tramos activos
    public CamionResponseDTO deshabilitar(@NonNull String dominio) {
        log.info("Deshabilitando camión con dominio {}", dominio);

        Camion camion = camionRepository.findById(dominio)
                .orElseThrow(() -> new NoSuchElementException("Camión no encontrado"));

        if (!camion.getDisponibilidad()) {
            log.debug("El camión {} ya estaba deshabilitado", dominio);
            return new CamionResponseDTO(dominio, "El camión ya estaba deshabilitado");
        }

        camion.setDisponibilidad(false);
        camionRepository.save(camion);

        log.info("Camión {} deshabilitado correctamente", dominio);

        return new CamionResponseDTO(dominio, "Camión deshabilitado correctamente");
    }
}