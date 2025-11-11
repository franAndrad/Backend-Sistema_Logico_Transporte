package ar.edu.utn.frc.backend.logistica.ms_cliente.repository;

import ar.edu.utn.frc.backend.logistica.ms_cliente.entities.Contenedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContenedorRepository extends JpaRepository<Contenedor, Integer> {
    // Buscar por el id del cliente (propiedad anidada)
    List<Contenedor> findByCliente_IdCliente(Integer idCliente);
    boolean existsByIdentificacion(String identificacion);

    List<Contenedor> findByActivoTrue();
    Optional<Contenedor> findByIdContenedorAndActivoTrue(Integer idContenedor);
    List<Contenedor> findByCliente_IdClienteAndActivoTrue(Integer idCliente);
}
