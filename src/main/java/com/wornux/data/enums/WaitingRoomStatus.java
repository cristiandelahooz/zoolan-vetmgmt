package com.wornux.data.enums;

public enum WaitingRoomStatus {
  ESPERANDO("Esperando"),
  EN_CONSULTA("En Consulta"),
  COMPLETADO("Completado"),
  CANCELADO("Cancelado");

  private final String displayName;

  WaitingRoomStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
