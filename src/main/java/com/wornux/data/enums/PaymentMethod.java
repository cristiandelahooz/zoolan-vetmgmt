package com.wornux.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
  CASH("Efectivo"),
  ELECTRONIC("Electronico"), // Credit Card, PayPal, Stripe, etc.
  TRANSFER("Transferencia");

  private final String display;
}
