package com.wornux.components;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class QuantityComponent extends Span {

    public QuantityComponent() {
        addClassNames(LumoUtility.BorderRadius.SMALL, LumoUtility.FontWeight.MEDIUM, LumoUtility.JustifyContent.CENTER,
                LumoUtility.AlignItems.CENTER, LumoUtility.Padding.XSMALL, LumoUtility.Padding.Horizontal.SMALL,
                LumoUtility.TextColor.PRIMARY_CONTRAST, LumoUtility.Background.PRIMARY, LumoUtility.Display.HIDDEN,
                LumoUtility.Display.Breakpoint.Large.FLEX);
        setMinWidth(10, Unit.REM);
        setHeight(1.2F, Unit.REM);
    }
}
