package me.winflix.vitalcore.structures.interfaces;

public enum BuildStatus {
    NOT_STARTED,   // No se ha comenzado a construir
    IN_PROGRESS,   // La construcción está en curso
    COMPLETED,     // La construcción ha sido completada
    FAILED,        // La construcción ha fallado (por ejemplo, si ya existe algo en el lugar)
    CANCELLED      // La construcción fue cancelada
}