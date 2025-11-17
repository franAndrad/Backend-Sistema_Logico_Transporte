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
@Table(name = "camiones")
public class Camion {
    @Id
    private String dominio;

    @Column(name = "CAPACIDAD", nullable = false)
    private Float capacidad;

    @Column(name = "VOLUMEN", nullable = false)
    private Float volumen;

    @Column(name = "DISPONIBILIDAD", nullable = false)
    private Boolean disponibilidad;

    @Column(name = "COSTO_BASE", nullable = false)
    @DecimalMin(value = "0.01", message = "El costo debe ser mayor a 0")
    private BigDecimal costoBase;

    @Column(name = "CONSUMO_COMBUSTIBLE", nullable = false)
    @DecimalMin(value = "0.01", message = "El consumo debe ser mayor a 0")
    private Float consumoCombustible;
}
