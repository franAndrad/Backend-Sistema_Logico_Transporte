package ar.edu.utn.frc.backend.logistica.ms_transporte.repository;

import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Camion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CamionRepository extends JpaRepository<Camion, String> {
    boolean existsByDominio(String dominio);
    boolean existsByDisponibilidad(Boolean disponibilidad);
    List<Camion> findByDisponibilidadTrue();
}
