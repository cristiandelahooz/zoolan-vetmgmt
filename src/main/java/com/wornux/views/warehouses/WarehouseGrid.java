package com.wornux.views.warehouses;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.Warehouse;
import com.wornux.data.enums.WarehouseType;
import com.wornux.utils.NotificationUtils;
import java.util.function.Consumer;

public class WarehouseGrid extends Grid<Warehouse> {
    public WarehouseGrid(ListDataProvider<Warehouse> dataProvider, Consumer<Warehouse> onEdit,
            Consumer<Warehouse> onDelete) {
        super(Warehouse.class, false);
        setWidthFull();
        setHeight("450px");
        setDataProvider(dataProvider);

        addColumn(Warehouse::getName).setWidth("200px").setHeader("Nombre");
        addComponentColumn(warehouse -> renderWarehouseType(warehouse.getWarehouseType())).setWidth("150px").setHeader(
                "Tipo de Almacén");
        addComponentColumn(warehouse -> renderAvailableForSale(warehouse.isAvailableForSale())).setWidth("150px")
                .setHeader("Disponible para Venta").setTextAlign(ColumnTextAlign.CENTER).addClassNames(
                        LumoUtility.JustifyContent.CENTER);
        addComponentColumn(warehouse -> renderStatus(warehouse.isStatus())).setWidth("120px").setHeader("Estado");
        addComponentColumn(warehouse -> createActionsColumn(warehouse, onEdit, onDelete)).setWidth("70px").setHeader(
                "Acciones").setTextAlign(ColumnTextAlign.START).addClassNames(LumoUtility.JustifyContent.START);

        addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
    }

    private Component createActionsColumn(Warehouse warehouse, Consumer<Warehouse> onEdit,
            Consumer<Warehouse> onDelete) {
        Button edit = new Button(new Icon(VaadinIcon.EDIT));
        edit.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        edit.getElement().setProperty("title", "Editar");
        edit.getStyle().set("min-width", "32px").set("width", "32px").set("padding", "0");

        Button delete = new Button(new Icon(VaadinIcon.TRASH));
        delete.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL,
                ButtonVariant.LUMO_ERROR);
        delete.getElement().setProperty("title", "Eliminar");
        delete.getStyle().set("min-width", "32px").set("width", "32px").set("padding", "0");

        edit.addClickListener(e -> onEdit.accept(warehouse));
        delete.addClickListener(e -> showDeleteConfirmationDialog(warehouse, onDelete));

        HorizontalLayout actions = new HorizontalLayout(edit, delete);
        actions.setSpacing(true);
        actions.setPadding(false);
        actions.setMargin(false);
        actions.setWidth(null);
        return actions;
    }

    private void showDeleteConfirmationDialog(Warehouse warehouse, Consumer<Warehouse> onDelete) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirmar eliminación");
        confirmDialog.setModal(true);
        confirmDialog.setWidth("400px");

        Span message = new Span("¿Está seguro de que desea eliminar el almacén \"" + (warehouse
                .getName() != null ? warehouse.getName() : "") + "\"? Esta acción no se puede deshacer.");
        message.getStyle().set("margin-bottom", "20px");

        Button confirmButton = new Button("Eliminar");
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirmButton.addClickListener(e -> {
            try {
                onDelete.accept(warehouse);
                NotificationUtils.success("Almacén eliminado exitosamente");
                confirmDialog.close();
            } catch (Exception ex) {
                NotificationUtils.error("Error al eliminar el almacén: " + ex.getMessage());
            }
        });

        Button cancelButton = new Button("Cancelar");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(e -> confirmDialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, confirmButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setSpacing(true);

        VerticalLayout content = new VerticalLayout(message, buttonLayout);
        content.setPadding(false);
        content.setSpacing(true);

        confirmDialog.add(content);
        confirmDialog.open();
    }

    private Component renderWarehouseType(WarehouseType type) {
        if (type == null)
            return new Span("-");
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
