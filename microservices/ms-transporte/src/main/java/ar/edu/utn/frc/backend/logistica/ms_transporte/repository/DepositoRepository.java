package ar.edu.utn.frc.backend.logistica.ms_transporte.repository;

import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Deposito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepositoRepository extends JpaRepository<Deposito, Integer> {
    boolean existsByNombre(String nombre);
    boolean existsByDireccion(String direccion);
    List<Deposito> findByActivoTrue();
}