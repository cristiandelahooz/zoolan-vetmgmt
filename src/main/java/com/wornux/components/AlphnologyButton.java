package com.wornux.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;

public enum AlphnologyButton {
    SAVE, NEW, FIND, DELETE, PRINT, CANCEL, FIRST, PREVIOUS, NEXT, LAST;

    private final String title;
    private final String collection;
    private final String icon;

    AlphnologyButton() {
        this.title = getTitle(name().toUpperCase());
        this.collection = getCollection(name().toUpperCase());
        this.icon = getIcon(name().toUpperCase());
    }

    public Button create() {
        Button button = new Button(title, new Icon(collection, icon));
        button.getElement().setAttribute("theme", "btn primary %s".formatted(getTheme(name().toUpperCase())));
        return button;
    }

    private String getTheme(String name) {
        return switch (name) {
        case "SAVE", "NEW", "FIND", "DELETE", "PRINT", "CANCEL" -> name.toLowerCase();
        case "FIRST", "PREVIOUS", "NEXT", "LAST" -> "displacement";
        default -> "";
        };
    }

    private String getTitle(String name) {
        return switch (name) {
        case "SAVE" -> "Save";
        case "NEW" -> "New";
        case "FIND" -> "Find";
        case "PRINT" -> "Print";
        case "CANCEL" -> "Cancel";
        case "DELETE" -> "Delete";
        case "FIRST" -> "First";
        case "PREVIOUS" -> "Previous";
        case "NEXT" -> "Next";
        case "LAST" -> "Last";
        default -> "";
        };
    }

    private String getIcon(String name) {
        return switch (name) {
        case "SAVE" -> "harddrive-o";
        case "NEW" -> "file-add";
        case "FIND" -> "search";
        case "PRINT" -> "print";
        case "CANCEL" -> "close";
        case "DELETE" -> "trash";
        case "FIRST" -> "arrow-left";
        case "PREVIOUS" -> "chevron-left";
        case "NEXT" -> "chevron-right";
        case "LAST" -> "arrow-right";
        default -> "";
        };
    }

    private String getCollection(String name) {
        return switch (name) {
        case "SAVE", "NEW", "FIND", "PRINT", "DELETE", "CANCEL" -> "vaadin";
        case "FIRST", "PREVIOUS", "NEXT", "LAST" -> "lumo";
        default -> "";
        };
    }

}
