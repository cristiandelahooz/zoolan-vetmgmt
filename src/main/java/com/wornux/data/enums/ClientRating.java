package com.wornux.data.enums;

public enum ClientRating {
  MUY_BUENO("Cliente muy bueno"),
  BUENO("Cliente bueno"),
  REGULAR("Cliente regular"),
  PAGO_TARDIO("Cliente con pagos tardíos"),
  CONFLICTIVO("Cliente conflictivo");

  private final String description;

  ClientRating(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
