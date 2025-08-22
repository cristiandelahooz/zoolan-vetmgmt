package com.wornux.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.theme.lumo.LumoUtility.*;

public class ListItem extends com.vaadin.flow.component.html.ListItem {

    // Components
    protected Div prefix;
    protected Div column;
    protected Div primary;
    protected Div secondary;
    protected Div suffix;

    public ListItem() {
        addClassNames(AlignItems.CENTER, Background.BASE, Display.FLEX, Gap.MEDIUM, Padding.Horizontal.MEDIUM,
                Padding.Vertical.SMALL, Position.RELATIVE);

        this.prefix = new Div();
        this.prefix.addClassNames(Display.FLEX);
        this.prefix.setVisible(false);

        this.primary = new Div();
        this.primary.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.SMALL);
        this.primary.setVisible(false);

        this.secondary = new Div();
        this.secondary.addClassNames(Display.FLEX, FontSize.SMALL, TextColor.SECONDARY);
        this.secondary.setVisible(false);

        this.column = new Div(this.primary, this.secondary);
        this.column.addClassNames(Display.FLEX, Padding.Vertical.XSMALL, FlexDirection.COLUMN, Flex.GROW);

        this.suffix = new Div();
        this.suffix.addClassNames(Display.FLEX, Gap.SMALL);
        this.suffix.setVisible(false);

        add(this.prefix, this.column, this.suffix);
    }

    public ListItem(String primary, String secondary) {
        this(null, new Text(primary), new Text(secondary), null);
    }

    public ListItem(Component primary, Component secondary) {
        this(null, primary, secondary, null);
    }

    public ListItem(Component prefix, String primary, String secondary) {
        this(prefix, new Text(primary), new Text(secondary), null);
    }

    public ListItem(String primary, String secondary, Component suffix) {
        this(null, new Text(primary), new Text(secondary), suffix);
    }

    public ListItem(Component prefix, String primary, String secondary, Component suffix) {
        this(prefix, new Text(primary), new Text(secondary), suffix);
    }

    public ListItem(Component prefix, Component primary, Component secondary, Component suffix) {
        this();
        setPrefix(prefix);
        setPrimary(primary);
        setSecondary(secondary);
        setSuffix(suffix);
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

    /** Sets the primary content. */
    public void setPrimary(Component... components) {
        this.primary.removeAll();
        if (components != null) {
            for (Component component : components) {
                if (component != null) {
                    this.primary.add(component);
                }
            }
        }
        this.primary.setVisible(this.primary.getComponentCount() > 0);
    }

    /** Sets the primary content. */
    public void setPrimary(String text) {
        this.setPrimary(new Text(text));
    }

    /** Sets the secondary content. */
    public void setSecondary(String text) {
        this.setSecondary(new Text(text));
    }

    /** Sets the secondary content. */
    public void setSecondary(Component... components) {
        this.secondary.removeAll();
        if (components != null) {
            for (Component component : components) {
                if (component != null) {
                    this.secondary.add(component);
                }
            }
        }
        this.secondary.setVisible(this.secondary.getComponentCount() > 0);
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
