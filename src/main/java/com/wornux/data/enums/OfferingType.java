package com.wornux.data.enums;

import lombok.Getter;

/** Enum for categorizing services by type (Medical or Grooming) */
@Getter
public enum OfferingType {
  CONSULTATION("Consulta", "Consulta médica veterinaria"),
  VACCINATION("Vacunación", "Aplicación de vacunas"),
  SURGERY("Cirugía", "Procedimientos quirúrgicos"),
  EMERGENCY("Emergencia", "Atención de emergencia"),
  GROOMING("Estética", "Servicios de estética y cuidado personal"),
  MEDICAL("Médico", "Servicios médicos veterinarios generales");

  private final String display;
  private final String description;

  OfferingType(String display, String description) {
    this.display = display;
    this.description = description;
  }
}
