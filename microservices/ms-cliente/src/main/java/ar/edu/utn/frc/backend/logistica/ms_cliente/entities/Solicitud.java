package ar.edu.utn.frc.backend.logistica.ms_cliente.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "solicitudes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Solicitud {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Integer idSolicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false, referencedColumnName = "id_cliente")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contenedor", nullable = false, referencedColumnName = "id_contenedor")
    private Contenedor contenedor;

    @Column(name = "id_tarifa")
    private Integer tarifaId;

    @Column(name = "origen_direccion", nullable = false, length = 500)
    private String origenDireccion;

    @Column(name = "origen_latitud")
    private Double origenLatitud;

    @Column(name = "origen_longitud")
    private Double origenLongitud;

    @Column(name = "destino_direccion", nullable = false, length = 500)
    private String destinoDireccion;

    @Column(name = "destino_latitud")
    private Double destinoLatitud;

    @Column(name = "destino_longitud")
    private Double destinoLongitud;

    @Column(name = "costo_estimado")
    private Double costoEstimado;

    @Column(name = "tiempo_estimado")
    private Integer tiempoEstimado;

    @Column(name = "costo_final")
    private Double costoFinal;

    @Column(name = "tiempo_real")
    private Integer tiempoReal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SolicitudEstado estado = SolicitudEstado.BORRADOR;

    @Column(name = "descripcion_estado", length = 500)
    private String descripcionEstado;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = fechaCreacion;
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
