package com.wornux.views.warehouses;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.wornux.data.entity.Warehouse;
import com.wornux.data.enums.WarehouseType;

import java.util.function.Consumer;

public class WarehouseGrid extends Grid<Warehouse> {
    public WarehouseGrid(ListDataProvider<Warehouse> dataProvider, Consumer<Warehouse> onSelect) {
        super(Warehouse.class, false);
        setWidthFull();
        setHeight("450px");
        setDataProvider(dataProvider);

        addColumn(Warehouse::getName).setAutoWidth(true).setHeader("Nombre");
        addComponentColumn(warehouse -> renderWarehouseType(warehouse.getWarehouseType()))
                .setAutoWidth(true).setHeader("Tipo de Almacén");
        addComponentColumn(warehouse -> renderAvailableForSale(warehouse.isAvailableForSale()))
                .setAutoWidth(true).setHeader("Disponible para Venta");
        addComponentColumn(warehouse -> renderStatus(warehouse.isStatus()))
                .setAutoWidth(true).setHeader("Estado");

        addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(onSelect));
    }

    // WarehouseType badge renderer
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

    // Disponible para Venta badge renderer
    private Component renderAvailableForSale(Boolean available) {
        Span badge = new Span(Boolean.TRUE.equals(available) ? "Sí" : "No");
        badge.getElement().getThemeList().add("badge pill");
        badge.getElement().getThemeList().add(Boolean.TRUE.equals(available) ? "success" : "error");
        return badge;
    }

    // Estado badge renderer
    private Component renderStatus(Boolean status) {
        Span badge = new Span(Boolean.TRUE.equals(status) ? "Activo" : "Inactivo");
        badge.getElement().getThemeList().add("badge pill");
        badge.getElement().getThemeList().add(Boolean.TRUE.equals(status) ? "success" : "error");
        return badge;
    }
}