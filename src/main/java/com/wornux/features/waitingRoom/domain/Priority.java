package com.wornux.features.waitingRoom.domain;

public enum Priority {
    NORMAL("Normal", 1), URGENT("Urgente", 2), EMERGENCY("Emergencia", 3);

    private final String displayName;
    private final int numericValue;

    Priority(String displayName, int numericValue) {
        this.displayName = displayName;
        this.numericValue = numericValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getNumericValue() {
        return numericValue;
    }

    public static Priority fromNumericValue(int value) {
        for (Priority priority : values()) {
            if (priority.numericValue == value) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Invalid priority value: " + value);
    }

    @Override
    public String toString() {
        return displayName;
    }
}