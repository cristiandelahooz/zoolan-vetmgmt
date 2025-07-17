package com.wornux.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InvoiceStatus {

    PARTIAL("Partial"),
    OVERPAID("Overpaid"),
    PENDING("Pending"),
    PAID("Paid"),
    OVERDUE("Overdue"),
    DRAFT("Draft"),
    UNSENT("Unsent"),
    SENT("Sent");

    private final String display;

}
