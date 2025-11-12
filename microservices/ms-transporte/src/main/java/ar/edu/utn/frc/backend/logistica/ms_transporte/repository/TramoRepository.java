package ar.edu.utn.frc.backend.logistica.ms_transporte.repository;

import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Tramo;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TramoRepository extends JpaRepository<Tramo, Integer> {
    List<Tramo> findByRuta(Ruta ruta);
}
