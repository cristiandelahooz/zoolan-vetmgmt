package com.wornux.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import java.util.stream.Stream;
import org.vaadin.lineawesome.LineAwesomeIcon;

public class Badge extends Span implements HasTheme {

    private Component icon;

    public Badge() {
        addThemeName("badge");
    }

    public Badge(String text) {
        this();
        add(new Span(text));
    }

    public Badge(String text, BadgeVariant... variants) {
        this(text);
        addThemeVariants(variants);
    }

    public void setIcon(LineAwesomeIcon icon) {
        if (this.icon != null) {
            remove(this.icon);
        }
        this.icon = icon.create();
        this.icon.addClassNames(Padding.XSMALL);
        addComponentAsFirst(this.icon);
    }

    public void addThemeVariants(BadgeVariant... variants) {
        getThemeNames().addAll(Stream.of(variants).map(BadgeVariant::getVariantName).toList());
    }
}
