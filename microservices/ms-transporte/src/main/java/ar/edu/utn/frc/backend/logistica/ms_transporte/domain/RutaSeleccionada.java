package ar.edu.utn.frc.backend.logistica.ms_transporte.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "rutas_seleccionadas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutaSeleccionada {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "viaje_id")
    private Long viajeId;
    
    @Column(name = "cliente_id")
    private Long clienteId;
    
    @Column(name = "origen_lat", nullable = false)
    private Double origenLat;
    
    @Column(name = "origen_lng", nullable = false)
    private Double origenLng;
    
    @Column(name = "destino_lat", nullable = false)
    private Double destinoLat;
    
    @Column(name = "destino_lng", nullable = false)
    private Double destinoLng;
    
    @Column(name = "numero_ruta", nullable = false)
    private Integer numeroRuta;
    
    @Column(name = "distancia_km", nullable = false)
    private Double distanciaKm;
    
    @Column(name = "duracion_minutos", nullable = false)
    private Long duracionMinutos;
    
    @Column(name = "resumen", length = 500)
    private String resumen;
    
    @Column(name = "es_mas_rapida")
    private Boolean esMasRapida;
    
    @Column(name = "fecha_seleccion", nullable = false)
    private LocalDateTime fechaSeleccion;
    
    @Column(name = "estado")
    @Enumerated(EnumType.STRING)
    private EstadoRuta estado;
    
    @PrePersist
    protected void onCreate() {
        fechaSeleccion = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoRuta.SELECCIONADA;
        }
    }
}
