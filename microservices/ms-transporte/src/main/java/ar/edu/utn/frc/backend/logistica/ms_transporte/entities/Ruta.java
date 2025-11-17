package ar.edu.utn.frc.backend.logistica.ms_transporte.entities;

import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.enums.EstadoRuta;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "rutas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRuta;

    @NotNull(message = "El ID de la solicitud es obligatorio")
    @Column(name = "ID_SOLICITUD", nullable = false)
    private Integer idSolicitud; // referencia lógica a ms_cliente

    @Column(name = "CANTIDAD_TRAMOS", nullable = false)
    private Integer cantidadTramos = 0;

    @Column(name = "CANTIDAD_DEPOSITOS", nullable = false)
    private Integer cantidadDepositos = 0;

    @DecimalMin(value = "0.000", message = "La distancia total no puede ser negativa")
    @Column(name = "DISTANCIA_TOTAL", precision = 12, scale = 3, nullable = false)
    private BigDecimal distanciaTotal = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoRuta estado = EstadoRuta.ESTIMADA;

    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Tramo> tramos = new ArrayList<>();
}
