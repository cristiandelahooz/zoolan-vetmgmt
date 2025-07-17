package com.wornux.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.*;

/**
 * @author me@fredpena.dev
 * @created 30/03/2025  - 12:08
 */
public class MenuBarHandler {

    private static final String SELECTED_MENU_ITEM_BACKGROUND = LumoUtility.Background.PRIMARY;
    private static final String SELECTED_MENU_ITEM_TEXT_COLOR = LumoUtility.TextColor.PRIMARY_CONTRAST;

    private final MenuBar menuBar;
    private final Div contentContainer;
    private final Map<MenuItem, Component> menuItemToComponentMap = new HashMap<>();
    private final List<MenuItemSelectionListener> selectionListeners = new ArrayList<>();
    private MenuItem selectedMenuItem;

    public MenuBarHandler(MenuBar menuBar, Div contentContainer) {
        this.menuBar = menuBar;
        this.contentContainer = contentContainer;
    }

    /**
     * Adds a menu item and its associated component to the menu bar.
     *
     * @param menuItem
     *         The menu item to add.
     * @param component
     *         The component to display when the menu item is selected.
     */
    public void addMenuItem(MenuItem menuItem, Component component) {
        menuItemToComponentMap.put(menuItem, component);
        menuItem.addClickListener(event -> selectMenuItem(menuItem));
    }

    /**
     * Selects a menu item and displays its associated component.
     *
     * @param menuItem
     *         The menu item to select.
     */
    public void selectMenuItem(MenuItem menuItem) {
        if (Objects.equals(selectedMenuItem, menuItem)) {
            return;
        }

        if (selectedMenuItem != null) {
            selectedMenuItem.removeClassName(SELECTED_MENU_ITEM_BACKGROUND);
            selectedMenuItem.removeClassName(SELECTED_MENU_ITEM_TEXT_COLOR);
        }

        selectedMenuItem = menuItem;
        if (selectedMenuItem != null) {
            selectedMenuItem.addClassNames(SELECTED_MENU_ITEM_BACKGROUND, SELECTED_MENU_ITEM_TEXT_COLOR);
        }

        contentContainer.removeAll();
        Component component = menuItemToComponentMap.get(menuItem);
        if (component != null) {
            contentContainer.add(component);
        }
        fireMenuItemSelectionEvent(menuItem);
    }

    /**
     * Sets the default selected menu item.
     *
     * @param defaultMenuItem
     *         The menu item to select by default.
     */
    public void setDefaultMenuItem(MenuItem defaultMenuItem) {
        if (menuItemToComponentMap.containsKey(defaultMenuItem)) {
            selectMenuItem(defaultMenuItem);
        } else {
            throw new IllegalArgumentException("Default menu item is not in the menu bar");
        }
    }

    /**
     * Adds a listener to be notified when the selected menu item changes.
     *
     * @param listener
     *         The listener to add.
     */
    public void addMenuItemSelectionListener(MenuItemSelectionListener listener) {
        selectionListeners.add(listener);
    }

    /**
     * Removes a listener from the list of listeners.
     *
     * @param listener
     *         The listener to remove.
     */
    public void removeMenuItemSelectionListener(MenuItemSelectionListener listener) {
        selectionListeners.remove(listener);
    }

    private void fireMenuItemSelectionEvent(MenuItem selectedMenuItem) {
        for (MenuItemSelectionListener listener : selectionListeners) {
            listener.onMenuItemSelected(selectedMenuItem);
        }
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public Div getContentContainer() {
        return contentContainer;
    }

    public MenuItem getSelectedMenuItem() {
        return selectedMenuItem;
    }

    public void setSelectedMenuItem(MenuItem selectedMenuItem) {
        selectMenuItem(selectedMenuItem);
    }

    /**
     * Functional interface for listening to menu item selection events.
     */
    @FunctionalInterface
    public interface MenuItemSelectionListener {
        void onMenuItemSelected(MenuItem selectedMenuItem);
    }
}
