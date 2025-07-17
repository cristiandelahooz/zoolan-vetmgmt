package com.wornux.components;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * @author me@fredpena.dev
 * @created 23/12/2024  - 13:11
 */
public enum MiniCalendarVariant implements ThemeVariant {

    /** Highlight the background of weekend days. */
    HIGHLIGHT_WEEKEND("highlight-weekend"),
    /** Highlight the current day in the calendar view. */
    HIGHLIGHT_CURRENT_DAY("highlight-current-day"),
    /** Show the background of week days rounded. */
    ROUNDED("rounded"),
    /** Raise the week days when hovering. */
    HOVER_DAYS("hover");

    private final String variant;

    MiniCalendarVariant(String variant) {
        this.variant = variant;
    }

    @Override
    public String getVariantName() {
        return variant;
    }
}
