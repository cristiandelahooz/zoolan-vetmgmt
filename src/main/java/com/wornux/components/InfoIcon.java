package com.wornux.components;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.shared.Tooltip;

import java.util.Locale;

/**
 * @author me@fredpena.dev
 * @created 22/04/2025  - 18:17
 */
public enum InfoIcon {

    INFO_CIRCLE;

    public Icon create(String tooltipText) {
        Icon icon = new Icon("vaadin", name().toLowerCase(Locale.ENGLISH).replace('_', '-'));
        icon.setColor("var(--lumo-primary-color)");
        icon.setSize("var(--lumo-icon-size-m)");
        icon.getStyle().setMarginLeft("1.5rem");
        icon.setTooltipText(tooltipText).withPosition(Tooltip.TooltipPosition.END);
        return icon;
    }

}
