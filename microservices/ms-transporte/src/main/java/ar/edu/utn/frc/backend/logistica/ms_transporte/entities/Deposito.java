package ar.edu.utn.frc.backend.logistica.ms_transporte.entities;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Data;

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

    @Column(name = "LATITUD", precision = 11, scale = 8, nullable = false)

    private BigDecimal latitud;

    @Column(name = "LONGITUD", precision = 11, scale = 8, nullable = false)
    private BigDecimal longitud;
}
