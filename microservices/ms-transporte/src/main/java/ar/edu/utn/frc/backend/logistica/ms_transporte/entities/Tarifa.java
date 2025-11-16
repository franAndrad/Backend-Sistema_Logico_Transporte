package ar.edu.utn.frc.backend.logistica.ms_transporte.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tarifas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTarifa;

    @Column(name = "NOMBRE", nullable = false, length = 200)
    @NotBlank(message = "El concepto de la tarifa es obligatorio")
    private String concepto; // Mapeado a columna NOMBRE

    @Column(name = "VALOR_BASE", nullable = false)
    @DecimalMin(value = "0.01", message = "El valor base debe ser mayor a 0")
    private Float valorBase;

    @Column(name = "VALOR_POR_KM", nullable = false)
    @DecimalMin(value = "0.01", message = "El valor por km debe ser mayor a 0")
    private Float valorPorKm;

    @Column(name = "VALOR_POR_PESO", nullable = false)
    @DecimalMin(value = "0.01", message = "El valor por peso debe ser mayor a 0")
    private Float valorPorPeso;

    @Column(name = "VALOR_POR_VOLUMEN", nullable = false)
    @DecimalMin(value = "0.01", message = "El valor por volumen debe ser mayor a 0")
    private Float valorPorVolumen;

    @Column(name = "VALOR_POR_TRAMO", nullable = false)
    @DecimalMin(value = "0.01", message = "El valor por tramo debe ser mayor a 0")
    private Float valorPorTramo;

    @Column(name = "VALOR_LITRO_COMBUSTIBLE", nullable = false)
    @DecimalMin(value = "0.01", message = "El valor por litro de combustible debe ser mayor a 0")
    private Float valorLitroCombustible;

    @Column(name = "FECHA_VIGENCIA", nullable = false)
    @NotNull(message = "La fecha de vigencia es obligatoria")
    private LocalDate fechaVigencia;

    @Column(name = "ACTIVO", nullable = false)
    private Boolean activo = true;
}

