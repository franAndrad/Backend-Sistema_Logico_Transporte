package ar.edu.utn.frc.backend.logistica.ms_transporte.entities;

public enum EstadoRuta {
    PLANIFICADA,      // Ruta creada pero no asignada
    ASIGNADA,         // Asignada a un camión
    EN_TRANSITO,      // En curso
    COMPLETADA,       // Finalizada exitosamente
    CANCELADA         // Cancelada por algún motivo
}
