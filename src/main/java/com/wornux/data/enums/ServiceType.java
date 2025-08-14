package com.wornux.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ServiceType {
  CONSULTA_GENERAL("Consulta General"),
  CONSULTA_ESPECIALIZADA("Consulta Especializada"),
  VACUNACION("Vacunación"),
  DESPARASITACION("Desparasitación"),
  CIRUGIA_MENOR("Cirugía Menor"),
  CIRUGIA_MAYOR("Cirugía Mayor"),
  HOSPITALIZACION("Hospitalización"),
  EXAMENES_LABORATORIO("Exámenes de Laboratorio"),
  RADIOGRAFIA("Radiografía"),
  ECOGRAFIA("Ecografía"),
  EUTANASIA("Eutanasia"),

  // Servicios no clínicos
  GROOMING_BASICO("Grooming Básico"),
  GROOMING_COMPLETO("Grooming Completo"),
  BANO_MEDICADO("Baño Medicado"),
  CORTE_UNAS("Corte de Uñas"),
  LIMPIEZA_OIDOS("Limpieza de Oídos"),
  LIMPIEZA_DENTAL("Limpieza Dental"),

  // Otros servicios
  GUARDERIA("Guardería"),
  HOTEL_MASCOTAS("Hotel de Mascotas"),
  ENTRENAMIENTO("Entrenamiento"),
  CONSULTA_COMPORTAMIENTO("Consulta de Comportamiento");

  private final String displayName;

  public boolean isClinical() {
    return this == CONSULTA_GENERAL
        || this == CONSULTA_ESPECIALIZADA
        || this == VACUNACION
        || this == DESPARASITACION
        || this == CIRUGIA_MENOR
        || this == CIRUGIA_MAYOR
        || this == HOSPITALIZACION
        || this == EXAMENES_LABORATORIO
        || this == RADIOGRAFIA
        || this == ECOGRAFIA
        || this == EUTANASIA;
  }
}
