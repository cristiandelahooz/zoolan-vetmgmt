package com.wornux.views.warehouses;

import com.vaadin.flow.component.grid.Grid;
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
        addColumn(warehouse -> getWarehouseTypeDisplayName(warehouse.getWarehouseType()))
                .setAutoWidth(true).setHeader("Tipo de Almacén");
        addColumn(warehouse -> warehouse.isAvailableForSale() ? "Si" : "No")
                .setAutoWidth(true).setHeader("Disponible para Venta");
        addColumn(warehouse -> warehouse.isStatus() ? "Activo" : "Inactivo")
                .setAutoWidth(true).setHeader("Estado");

        addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(onSelect));
    }

    private String getWarehouseTypeDisplayName(WarehouseType type) {
        if (type == null)
            return "";
        return switch (type) {
            case PRINCIPAL -> "Principal";
            case SECUNDARIO -> "Secundario";
        };
    }
}