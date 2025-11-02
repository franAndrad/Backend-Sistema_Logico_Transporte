package ar.edu.utn.frc.backend.logistica.ms_transporte.entities;

public enum TipoTramo {
    ORIGEN_DEPOSITO,        // Cliente → Depósito
    DEPOSITO_DEPOSITO,      // Depósito → Depósito
    DEPOSITO_DESTINO,       // Depósito → Cliente final
    ORIGEN_DESTINO          // Cliente → Cliente final (directo)
}
