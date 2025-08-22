package com.wornux.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AppointmentStatus {
  PROGRAMADA("Programada"),
  EN_PROGRESO("En Progreso"),
  COMPLETADA("Completada"),
  CANCELADA("Cancelada"),
  NO_ASISTIO("No Asistió");

  private final String displayName;
}
