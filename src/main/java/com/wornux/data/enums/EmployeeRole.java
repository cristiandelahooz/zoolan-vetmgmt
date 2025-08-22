package com.wornux.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Specific roles for veterinary clinic employees */
@Getter
@RequiredArgsConstructor
public enum EmployeeRole {
  CLINIC_MANAGER("Gerente de Clínica", "Gestión general de la clínica", SystemRole.MANAGER),
  RECEPTIONIST("Recepcionista", "Gestión de citas y atención al cliente", SystemRole.USER),
  ADMINISTRATIVE("Administrativo", "Tareas administrativas y contabilidad", SystemRole.USER),

  VETERINARIAN("Veterinario/a", "Realiza consultas y procedimientos veterinarios", SystemRole.USER),
  GROOMER("Peluquero/a", "Servicios de peluquería y estética", SystemRole.USER),
  KENNEL_ASSISTANT(
      "Asistente de veterinario", "Cuidado de animales hospitalizados", SystemRole.USER),
  LAB_TECHNICIAN("Técnico de Laboratorio", "Análisis de laboratorio", SystemRole.USER);

  private final String displayName;
  private final String description;
  private final SystemRole systemRole;
}
