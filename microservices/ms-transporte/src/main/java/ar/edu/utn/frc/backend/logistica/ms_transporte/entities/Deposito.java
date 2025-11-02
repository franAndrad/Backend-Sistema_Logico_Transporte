package ar.edu.utn.frc.backend.logistica.ms_transporte.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "depositos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Deposito {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(nullable = false)
    private String direccion;
    
    @Column(nullable = false)
    private Double latitud;
    
    @Column(nullable = false)
    private Double longitud;
    
    @Column
    private String ciudad;
    
    @Column
    private String provincia;
    
    @Column
    private String codigoPostal;
    
    @Column
    private Boolean activo = true;
}
