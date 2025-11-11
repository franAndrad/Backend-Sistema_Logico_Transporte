package ar.edu.utn.frc.backend.logistica.ms_cliente.repository;

import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    boolean existsByKeycloakId(String keycloakId);
    boolean existsByCuit(String cuit);
    List<Cliente> findByActivoTrue();
    Optional<Cliente> findByIdClienteAndActivoTrue(Integer idCliente);
}