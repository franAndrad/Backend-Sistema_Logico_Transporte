package ar.edu.utn.frc.backend.logistica.ms_transporte.repository;

import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Integer> {
    List<Tarifa> findByActivoTrue();
    boolean existsByActivoTrue();
}

