package ar.edu.utn.frc.backend.logistica.ms_transporte.entities.enums;

public enum EstadoTramo {
    PLANIFICADO,
    ASIGNADO,
    INICIADO,
    FINALIZADO,
    CANCELADO;

    public boolean isIniciadoOFinalizado() {
        return this == INICIADO || this == FINALIZADO;
    }

    public boolean isEditable() {
        return this == PLANIFICADO || this == ASIGNADO;
    }

    public boolean isCancelado() {
        return this == CANCELADO;
    }
}
