package com.wornux.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {

    CASH("Cash"),
    ELECTRONIC("Electronic"), // Credit Card, PayPal, Stripe, etc.
    TRANSFER("Transfer");

    private final String display;
}
