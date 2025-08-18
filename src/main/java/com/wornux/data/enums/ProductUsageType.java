package com.wornux.data.enums;

public enum ProductUsageType {
    PRIVADO("Uso Interno"),
    VENTA("Para Venta"),
    AMBOS("Interno y Venta");

    private final String displayName;

    ProductUsageType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}