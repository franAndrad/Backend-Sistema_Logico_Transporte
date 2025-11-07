package ar.edu.utn.frc.backend.logistica.ms_transporte.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "depositos")
public class Deposito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDeposito;

    @Column(name = "NOMBRE", nullable = false, length = 80)
    private String nombre;

    @Column(name = "DIRECCION", nullable = false, length = 120)
    private String direccion;

    @Column(name = "COSTO_ESTADIA_DIARIO", nullable = false)
    @DecimalMin(value = "0.01", message = "El costo debe ser mayor a 0")
    private BigDecimal costoEstadiaDiario;

    @Column(name = "ACTIVO", nullable = false)
    private Boolean activo = true;

    @Column(name = "LATITUD", precision = 10, scale = 8, nullable = false)
    @DecimalMin(value = "-90.0", message = "Latitud mínima permitida -90")
    @DecimalMax(value = "90.0", message = "Latitud máxima permitida 90")
    private BigDecimal latitud;

    @Column(name = "LONGITUD", precision = 11, scale = 8, nullable = false)
    @DecimalMin(value = "-180.0", message = "Longitud mínima permitida -180")
    @DecimalMax(value = "180.0", message = "Longitud máxima permitida 180")
    private BigDecimal longitud;
}
