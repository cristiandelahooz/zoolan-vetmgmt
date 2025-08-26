package com.wornux.data.enums;

import lombok.Getter;

/** Enum for categorizing services by type (Medical or Grooming) */
@Getter
public enum OfferingType {
  VACCINATION("Vacunación", "Aplicación de vacunas"),
  GROOMING("Estética", "Servicios de estética y cuidado personal"),
  MEDICAL("Médico", "Servicios médicos veterinarios generales");

  private final String display;
  private final String description;

  OfferingType(String displayName, String description) {
    this.display = displayName;
    this.description = description;
  }
}
