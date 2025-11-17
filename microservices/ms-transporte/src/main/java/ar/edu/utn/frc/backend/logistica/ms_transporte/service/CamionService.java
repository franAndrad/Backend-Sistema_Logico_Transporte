package ar.edu.utn.frc.backend.logistica.ms_transporte.service;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.camion.CamionCreateRequestDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.camion.CamionCreateResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.camion.CamionResponseDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.camion.CamionUpdateRequestDTO;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Camion;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.CamionRepository;
import lombok.NonNull;

@Service
public class CamionService {
    
    @Autowired
    private CamionRepository camionRepository;

    public List<Camion> findAll() {
        return camionRepository.findAll();
    }

    public List<Camion> findAllDisponibles() {
        return camionRepository.findByDisponibilidadTrue();
    }

    public Camion findByDominio(@NonNull String dominio) {
        return camionRepository.findById(dominio)
                .orElseThrow(() -> new IllegalStateException("Camión no encontrado"));
    }

    public Camion save(CamionCreateRequestDTO dto){
        if (camionRepository.existsByDominio(dto.getDominio())) {
            throw new IllegalStateException("Ya existe un camión con ese dominio");
        }

        Camion camion = new Camion();
        camion.setDominio(dto.getDominio());
        camion.setCapacidad(dto.getCapacidad());
        camion.setVolumen(dto.getVolumen());
        camion.setDisponibilidad(true);
        camion.setCostoBase(dto.getCostoBase());
        camion.setConsumoCombustible(dto.getConsumoCombustible());

        return camionRepository.save(camion);
    }

    public CamionCreateResponseDTO actualizarCamion(@NonNull String dominio, CamionUpdateRequestDTO dto) {
        Camion camion = camionRepository.findById(dominio)
            .orElseThrow(() -> new NoSuchElementException("Camión no encontrado"));

        if (!camion.getDisponibilidad()) {
            throw new IllegalStateException("No se puede actualizar un camión no disponible");
        }

        camion.setCapacidad(dto.getCapacidad());
        camion.setVolumen(dto.getVolumen());
        camion.setCostoBase(dto.getCostoBase());
        camion.setConsumoCombustible(dto.getConsumoCombustible());

        camionRepository.save(camion);

        return new CamionCreateResponseDTO(camion.getDominio(), "Camión actualizado con éxito");
    }

    public CamionResponseDTO habilitar(@NonNull String dominio) {
        Camion camion = camionRepository.findById(dominio)
                .orElseThrow(() -> new NoSuchElementException("Camión no encontrado"));

        if (!camion.getDisponibilidad()) {
            return new CamionResponseDTO(dominio, "El camión ya estaba habilitado");
        }

        camion.setDisponibilidad(true);
        camionRepository.save(camion);

        return new CamionResponseDTO(dominio, "Camión habilitado correctamente");
    }

    public CamionResponseDTO deshabilitar(@NonNull String dominio) {
        Camion camion = camionRepository.findById(dominio)
                .orElseThrow(() -> new NoSuchElementException("Camión no encontrado"));

        if (!camion.getDisponibilidad()) {
            return new CamionResponseDTO(dominio, "El camión ya estaba deshabilitado");
        }

        camion.setDisponibilidad(false);
        camionRepository.save(camion);

        return new CamionResponseDTO(dominio, "Camión deshabilitado correctamente");
    }
}
