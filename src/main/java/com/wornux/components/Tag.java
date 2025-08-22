package com.wornux.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIcon;

public class Tag extends Span {

    private final Div prefix;
    private final Text text;

    public Tag(Component prefix, String text, String color) {
        addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.Display.FLEX, LumoUtility.FontSize.SMALL,
                LumoUtility.Gap.SMALL, color);

        this.prefix = new Div();
        this.prefix.addClassNames(LumoUtility.Display.FLEX);
        this.prefix.setVisible(false);
        setPrefix(prefix);

        this.text = new Text(text);

        add(this.prefix, this.text);
    }

    public Tag(Component prefix, String text) {
        this(prefix, text, LumoUtility.TextColor.SECONDARY);
    }

    public Tag(LineAwesomeIcon icon, String text, String color) {
        this(createIcon(icon), text, color);
    }

    public Tag(LineAwesomeIcon icon, String text) {
        this(icon, text, LumoUtility.TextColor.SECONDARY);
    }

    public Tag(String text) {
        this((Component) null, text, LumoUtility.TextColor.SECONDARY);
    }

    private static Component createIcon(LineAwesomeIcon icon) {
        SvgIcon i = icon.create();
        i.addClassNames(LumoUtility.IconSize.SMALL);
        return i;
    }

    /** Sets the prefix. */
    public void setPrefix(Component... components) {
        this.prefix.removeAll();
        if (components != null) {
            for (Component component : components) {
                if (component != null) {
                    if (component instanceof Icon) {
                        component.addClassNames(LumoUtility.IconSize.SMALL);
                    }
                    if (component instanceof Avatar) {
                        ((Avatar) component).addThemeVariants(AvatarVariant.LUMO_XSMALL);
                    }
                    this.prefix.add(component);
                }
            }
        }
        this.prefix.setVisible(this.prefix.getComponentCount() > 0);
    }
}
