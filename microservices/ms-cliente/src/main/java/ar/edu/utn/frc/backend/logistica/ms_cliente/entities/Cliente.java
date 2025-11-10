package ar.edu.utn.frc.backend.logistica.ms_cliente.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer idCliente;

    @Column(name = "keycloak_id", nullable = false)
    private String keycloakId;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String telefono;

    @Column(name = "direccion_facturacion", nullable = false)
    private String direccionFacturacion;

    @Column(name = "direccion_envio")
    private String direccionEnvio;

    @Column(name = "razon_social")
    private String razonSocial;

    @Column(length = 11)
    private String cuit;

    @Column(nullable = false)
    private Boolean activo = true;
}