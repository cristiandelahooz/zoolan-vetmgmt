package com.wornux.features.appointments.domain;

public enum AppointmentStatus {
    PROGRAMADA("Programada"),
    CONFIRMADA("Confirmada"), 
    EN_PROGRESO("En Progreso"),
    COMPLETADA("Completada"),
    CANCELADA("Cancelada"),
    NO_ASISTIO("No Asistió");
    
    private final String displayName;
    
    AppointmentStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}