package ar.edu.utn.frc.backend.logistica.ms_transporte.repository;

import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Integer> {
    Optional<Ruta> findByIdSolicitud(Integer idSolicitud);
}
