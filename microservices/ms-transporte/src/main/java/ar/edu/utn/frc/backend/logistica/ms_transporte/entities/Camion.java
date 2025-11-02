package ar.edu.utn.frc.backend.logistica.ms_transporte.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "camiones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Camion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String patente;
    
    @Column(nullable = false)
    private String marca;
    
    @Column(nullable = false)
    private String modelo;
    
    @Column
    private Integer anio;
    
    @Column(nullable = false)
    private Double capacidadKg;
    
    @Column(nullable = false)
    private Double capacidadM3;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCamion tipo;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCamion estado = EstadoCamion.DISPONIBLE;
    
    @Column
    private Double costoPorKm;
    
    @Column
    private Boolean activo = true;
}
