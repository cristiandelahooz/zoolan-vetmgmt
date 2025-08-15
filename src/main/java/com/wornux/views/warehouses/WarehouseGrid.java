package com.wornux.views.warehouses;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.Warehouse;
import com.wornux.data.enums.WarehouseType;

import java.util.function.Consumer;

public class WarehouseGrid extends Grid<Warehouse> {
    public WarehouseGrid(ListDataProvider<Warehouse> dataProvider, Consumer<Warehouse> onEdit, Consumer<Warehouse> onDelete) {
        super(Warehouse.class, false);
        setWidthFull();
        setHeight("450px");
        setDataProvider(dataProvider);

        addColumn(Warehouse::getName).setWidth("200px").setHeader("Nombre");
        addComponentColumn(warehouse -> renderWarehouseType(warehouse.getWarehouseType())).setWidth("150px").setHeader("Tipo de Almacén");
        addComponentColumn(warehouse -> renderAvailableForSale(warehouse.isAvailableForSale())).setWidth("150px").setHeader("Disponible para Venta").setTextAlign(ColumnTextAlign.CENTER).addClassNames(LumoUtility.JustifyContent.CENTER);
        addComponentColumn(warehouse -> renderStatus(warehouse.isStatus())).setWidth("120px").setHeader("Estado");
        addComponentColumn(warehouse -> createActionsColumn(warehouse, onEdit, onDelete)).setWidth("70px").setHeader("Acciones").setTextAlign(ColumnTextAlign.CENTER).addClassNames(LumoUtility.JustifyContent.CENTER);

        addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
    }

    private Component createActionsColumn(Warehouse warehouse, Consumer<Warehouse> onEdit, Consumer<Warehouse> onDelete) {
        Button editButton = new Button(new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editButton.addClickListener(e -> onEdit.accept(warehouse));
        editButton.getElement().setAttribute("aria-label", "Editar");

        Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> onDelete.accept(warehouse));
        deleteButton.getElement().setAttribute("aria-label", "Eliminar");

        HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton);
        actions.setSpacing(false);
        actions.setPadding(false);
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        return actions;
    }

    private Component renderWarehouseType(WarehouseType type) {
        if (type == null) return new Span("-");
        Span badge = new Span(type.name());
        badge.getElement().getThemeList().add("badge pill");
        switch (type) {
            case PRINCIPAL -> badge.getElement().getThemeList().add("primary");
            case SECUNDARIO -> badge.getElement().getThemeList().add("contrast");
            default -> badge.getElement().getThemeList().add("primary");
        }
        return badge;
    }

    private Component renderAvailableForSale(Boolean available) {
        Span badge = new Span(Boolean.TRUE.equals(available) ? "Sí" : "No");
        badge.getElement().getThemeList().add("badge pill");
        badge.getElement().getThemeList().add(Boolean.TRUE.equals(available) ? "success" : "error");
        return badge;
    }

    private Component renderStatus(Boolean status) {
        Span badge = new Span(Boolean.TRUE.equals(status) ? "Activo" : "Inactivo");
        badge.getElement().getThemeList().add("badge pill");
        badge.getElement().getThemeList().add(Boolean.TRUE.equals(status) ? "success" : "error");
        return badge;
    }
}