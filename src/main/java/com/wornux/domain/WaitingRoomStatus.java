package com.wornux.domain;

public enum WaitingRoomStatus {
    WAITING("Esperando"), IN_CONSULTATION("En Consulta"), COMPLETED("Completado"), CANCELLED("Cancelado");

    private final String displayName;

    WaitingRoomStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}