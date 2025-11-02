package ar.edu.utn.frc.backend.logistica.ms_transporte.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un tramo individual de una ruta
 * Ejemplos:
 * - Origen (cliente) → Depósito A
 * - Depósito A → Depósito B
 * - Depósito B → Destino (cliente final)
 * - Origen → Destino (directo, sin depósitos)
 */
@Entity
@Table(name = "tramos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tramo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;
    
    @Column(nullable = false)
    private Integer orden;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTramo tipo;
    
    // Punto de inicio del tramo
    @Column(nullable = false)
    private Double inicioLatitud;
    
    @Column(nullable = false)
    private Double inicioLongitud;
    
    @Column
    private String inicioDireccion;
    
    // Punto final del tramo
    @Column(nullable = false)
    private Double finLatitud;
    
    @Column(nullable = false)
    private Double finLongitud;
    
    @Column
    private String finDireccion;
    
    // Depósito asociado (si aplica)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deposito_origen_id")
    private Deposito depositoOrigen;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deposito_destino_id")
    private Deposito depositoDestino;
    
    // Métricas calculadas por Google Maps
    @Column(nullable = false)
    private Double distanciaKm;
    
    @Column(nullable = false)
    private Long duracionMinutos;
    
    @Column
    private Double costoPorKm;
    
    @Column
    private Double costoTramo;
    
    // Método para calcular costo
    public void calcularCosto() {
        if (costoPorKm != null && distanciaKm != null) {
            this.costoTramo = costoPorKm * distanciaKm;
        }
    }
}
