package ar.edu.utn.frc.backend.logistica.ms_cliente.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "contenedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contenedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contenedor")
    private Integer idContenedor;

    @Column(nullable = false, unique = true, length = 100)
    private String identificacion;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false, referencedColumnName = "id_cliente")
    private Cliente cliente;

    @Column(nullable = false)
    private Double peso;

    @Column(nullable = false)
    private Double volumen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContenedorEstado estado = ContenedorEstado.EN_ORIGEN;

    @Column(name = "ubicacion_actual")
    private String ubicacionActual;

    @Column(nullable = false)
    private Boolean activo = true;
}
