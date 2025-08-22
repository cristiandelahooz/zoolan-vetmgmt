package com.wornux.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class BoardCard extends Div {
    // Components
    private final Div prefix;
    private final Component value;
    private final Div details;
    private final Div suffix;

    public BoardCard(String heading, String value) {
        this(null, heading, value, null);
    }

    public BoardCard(Component prefix, String heading, String value) {
        this(prefix, heading, value, null);
    }

    public BoardCard(Component prefix, String heading, String value, Component suffix) {
        addClassNames(LumoUtility.Border.RIGHT, LumoUtility.Border.LEFT, LumoUtility.BorderRadius.NONE,
                LumoUtility.Display.FLEX, LumoUtility.Background.BASE, LumoUtility.Padding.Horizontal.MEDIUM,
                LumoUtility.Padding.Vertical.SMALL, LumoUtility.AlignItems.CENTER, LumoUtility.Gap.MEDIUM,
                LumoUtility.Position.RELATIVE, "md:divide-x");

        this.prefix = new Div();
        this.prefix.addClassNames(LumoUtility.Display.FLEX);
        setPrefix(prefix);

        Component heading1 = new H3(heading);
        heading1.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.FontWeight.NORMAL,
                LumoUtility.TextColor.SECONDARY);

        this.value = new Span(value);
        this.value.addClassNames(LumoUtility.FontWeight.MEDIUM);
        this.value.addClassNames(LumoUtility.FontSize.XLARGE);

        this.details = new Div();
        this.details.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexWrap.WRAP, LumoUtility.Gap.SMALL);
        setDetails(null);

        Div column = new Div(heading1, this.value, this.details);
        column.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Padding.Vertical.XSMALL,
                LumoUtility.FlexDirection.COLUMN, LumoUtility.Flex.GROW);

        this.suffix = new Div();
        this.suffix.addClassNames(LumoUtility.Display.FLEX);
        setSuffix(suffix);

        add(this.prefix, column, this.suffix);
    }

    /** Sets the prefix. */
    public void setPrefix(Component... components) {
        this.prefix.removeAll();
        if (components != null) {
            for (Component component : components) {
                if (component != null) {
                    this.prefix.add(component);
                }
            }
        }
        this.prefix.setVisible(this.prefix.getComponentCount() > 0);
    }

    /** Sets the value. */
    public void setValue(String value) {
        this.value.getElement().setText(value);
    }

    /** Sets the details. */
    public void setDetails(Component... components) {
        this.details.removeAll();
        if (components != null) {
            for (Component component : components) {
                if (component != null) {
                    this.details.add(component);
                }
            }
        }
        this.details.setVisible(this.details.getComponentCount() > 0);
    }

    public void setSuffix(Component... components) {
        this.suffix.removeAll();
        if (components != null) {
            for (Component component : components) {
                if (component != null) {
                    this.suffix.add(component);
                }
            }
        }
        this.suffix.setVisible(this.suffix.getComponentCount() > 0);
    }
}
