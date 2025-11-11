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

    @Column(name = "keycloak_id", nullable = false, unique = true)
    private String keycloakId;

    @Column(name = "direccion_facturacion", nullable = false, length = 500)
    private String direccionFacturacion;

    @Column(name = "direccion_envio", length = 500)
    private String direccionEnvio;

    @Column(name = "razon_social")
    private String razonSocial;

    @Column(length = 11, unique = true)
    private String cuit;

    @Column(nullable = false)
    private Boolean activo = true;
}