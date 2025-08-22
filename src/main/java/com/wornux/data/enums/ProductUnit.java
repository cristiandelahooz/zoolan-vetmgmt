package com.wornux.data.enums;

public enum ProductUnit {
  UNIDAD("Unidad"),
  DOCENA("Docena"),
  CAJA("Caja");

  private final String displayName;

  ProductUnit(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
