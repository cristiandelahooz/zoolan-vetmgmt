package com.wornux.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InvoiceStatus {
  PARTIAL("Parcial"),
  OVERPAID("Sobrepago"),
  PENDING("Pendiente"),
  PAID("Pago"),
  OVERDUE("Vencido"),
  DRAFT("Borrador"),
  UNSENT("No enviado"),
  SENT("Enviado");

  private final String display;
}
