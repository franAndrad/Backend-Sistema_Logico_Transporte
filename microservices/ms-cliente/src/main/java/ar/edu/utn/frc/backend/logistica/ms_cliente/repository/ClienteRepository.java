package ar.edu.utn.frc.backend.logistica.ms_cliente.repository;

import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

}