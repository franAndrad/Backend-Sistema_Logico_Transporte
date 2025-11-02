package ar.edu.utn.frc.backend.logistica.ms_transporte.repository;

import ar.edu.utn.frc.backend.logistica.ms_transporte.domain.EstadoRuta;
import ar.edu.utn.frc.backend.logistica.ms_transporte.domain.RutaSeleccionada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RutaSeleccionadaRepository extends JpaRepository<RutaSeleccionada, Long> {
    
    List<RutaSeleccionada> findByClienteId(Long clienteId);
    
    Optional<RutaSeleccionada> findByViajeId(Long viajeId);
    
    List<RutaSeleccionada> findByEstado(EstadoRuta estado);
}
