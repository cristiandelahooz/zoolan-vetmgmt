package com.wornux.components;

import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.checkbox.Checkbox;

public class ToggleButton extends Checkbox implements HasTheme {
    public static final String THEME_NAME = "toggle-button";

    public ToggleButton() {
        this.addThemeName();
    }

    public ToggleButton(boolean initialValue) {
        super(initialValue);
        this.addThemeName();
    }

    public ToggleButton(String labelText, boolean initialValue) {
        super(labelText, initialValue);
        this.addThemeName();
    }

    public ToggleButton(String label, ValueChangeListener<ComponentValueChangeEvent<Checkbox, Boolean>> listener) {
        super(label, listener);
        this.addThemeName();
    }

    public ToggleButton(String labelText) {
        super(labelText);
        this.addThemeName();
    }

    private void addThemeName() {
        this.addThemeName(THEME_NAME);
    }
}