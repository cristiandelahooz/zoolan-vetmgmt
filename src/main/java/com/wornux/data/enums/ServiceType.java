package com.wornux.data.enums;

import lombok.Getter;

/** Enum for categorizing services by type (Medical or Grooming) */
@Getter
public enum ServiceType {
  MEDICAL("Médico", "Servicios médicos veterinarios"),
  GROOMING("Estética", "Servicios de estética y cuidado personal");

  private final String display;
  private final String description;

  ServiceType(String display, String description) {
    this.display = display;
    this.description = description;
  }
}
