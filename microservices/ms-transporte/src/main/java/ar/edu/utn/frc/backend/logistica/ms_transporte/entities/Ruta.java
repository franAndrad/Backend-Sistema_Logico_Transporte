package ar.edu.utn.frc.backend.logistica.ms_transporte.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una ruta completa desde origen hasta destino
 * Puede incluir múltiples tramos (origen->depósito, depósito->depósito, depósito->destino)
 */
@Entity
@Table(name = "rutas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ruta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String codigo;
    
    // Origen (del cliente)
    @Column(nullable = false)
    private Double origenLatitud;
    
    @Column(nullable = false)
    private Double origenLongitud;
    
    @Column
    private String origenDireccion;
    
    // Destino (cliente final)
    @Column(nullable = false)
    private Double destinoLatitud;
    
    @Column(nullable = false)
    private Double destinoLongitud;
    
    @Column
    private String destinoDireccion;
    
    // Métricas calculadas (suma de todos los tramos)
    @Column
    private Double distanciaTotalKm;
    
    @Column
    private Long duracionTotalMinutos;
    
    @Column
    private Double costoEstimado;
    
    // Relación con los tramos
    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tramo> tramos = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRuta estado = EstadoRuta.PLANIFICADA;
    
    @Column
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    @Column
    private LocalDateTime fechaActualizacion;
    
    // Métodos de utilidad
    public void agregarTramo(Tramo tramo) {
        tramos.add(tramo);
        tramo.setRuta(this);
    }
    
    public void calcularTotales() {
        this.distanciaTotalKm = tramos.stream()
            .mapToDouble(Tramo::getDistanciaKm)
            .sum();
        
        this.duracionTotalMinutos = tramos.stream()
            .mapToLong(Tramo::getDuracionMinutos)
            .sum();
    }
}
