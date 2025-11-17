package ar.edu.utn.frc.backend.logistica.ms_transporte.entities;

import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.enums.EstadoTramo;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.enums.TipoTramo;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "tramos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tramo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTramo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idRuta")
    @JsonBackReference
    private Ruta ruta;

    @Column(name = "ID_DEPOSITO_ORIGEN")
    private Integer idDepositoOrigen; 

    @Column(name = "ID_DEPOSITO_DESTINO")
    private Integer idDepositoDestino; 

    @Column(name = "KEYCLOAK_ID_TRANSPORTISTA", length = 80)
    private String keyCloakIdTransportista; 

    @Column(name = "DOMINIO_CAMION", length = 20)
    private String dominioCamion; 

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO", nullable = false, length = 30)
    private TipoTramo tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoTramo estado = EstadoTramo.PLANIFICADO;

    @DecimalMin(value = "0.000", message = "La distancia debe ser mayor o igual a 0")
    @Column(name = "DISTANCIA", precision = 12, scale = 3)
    private BigDecimal distancia;

    @DecimalMin(value = "0.00", message = "El costo aproximado no puede ser negativo")
    @Column(name = "COSTO_APROXIMADO", precision = 14, scale = 2)
    private BigDecimal costoAproximado;

    @DecimalMin(value = "0.00", message = "El costo real no puede ser negativo")
    @Column(name = "COSTO_REAL", precision = 14, scale = 2)
    private BigDecimal costoReal;

    @Column(name = "FH_INICIO_ESTIMADA")
    private LocalDateTime fechaHoraInicioEstimada;

    @Column(name = "FH_FIN_ESTIMADA")
    private LocalDateTime fechaHoraFinEstimada;

    @Column(name = "FH_INICIO")
    private LocalDateTime fechaHoraInicio;

    @Column(name = "FH_FIN")
    private LocalDateTime fechaHoraFin;

    @Column(name = "FH_ACTUALIZACION", nullable = false)
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @PrePersist
    @PreUpdate
    public void actualizarFecha() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
