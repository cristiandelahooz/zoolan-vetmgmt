package com.wornux.views.products;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.wornux.data.entity.Product;
import com.wornux.data.enums.ProductCategory;
import com.wornux.services.interfaces.ProductService;
import com.wornux.utils.NotificationUtils;
import org.springframework.data.domain.PageRequest;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.springframework.data.jpa.domain.Specification;

import java.util.function.Consumer;

public class ProductGrid extends Grid<Product> {
    private Specification<Product> specification;
    private final transient ProductService productService;

    private static final String COLUMN_WIDTH = "120px";


    public ProductGrid(ProductService productService,
                       Consumer<Product> onSelect) {
        super(Product.class, false);
        this.productService = productService;

        setItems(query -> {
            var products = productService.getAllProducts(PageRequest.of(query.getPage(), query.getPageSize(),
                    VaadinSpringDataHelpers.toSpringDataSort(query)));
            return products.stream().filter(Product::isActive);
        });

        addColumn(Product::getName).setWidth("150px").setHeader("Nombre");
        addColumn(product -> product.getDescription() != null ? product.getDescription() : "")
                .setWidth("200px").setHeader("Descripción");
        addColumn(product -> "$" + (product.getPurchasePrice() != null ? product.getPurchasePrice().toString() : "0.00"))
                .setWidth(COLUMN_WIDTH).setHeader("Precio de Compra");
        addColumn(product -> "$" + (product.getSalesPrice() != null ? product.getSalesPrice().toString() : "0.00"))
                .setWidth(COLUMN_WIDTH).setHeader("Precio de Venta");
        addColumn(Product::getAccountingStock).setWidth("100px").setHeader("Stock Contable");
        addColumn(Product::getAvailableStock).setWidth("100px").setHeader("Stock Disponible");
        addComponentColumn(this::renderCategoryBadge)
                .setHeader("Categoría")
                .setKey("categoryBadge")
                .setWidth(COLUMN_WIDTH);

        addColumn(product -> {
            try {
                return product.getSupplier() != null ? product.getSupplier().getCompanyName() : "Sin Proveedor";
            } catch (org.hibernate.LazyInitializationException e) {
                return "N/A";
            }
        }).setAutoWidth(true).setHeader("Proveedor");
        addColumn(product -> product.getWarehouse() != null ? product.getWarehouse().getName() : "Sin Almacén")
                .setAutoWidth(true).setHeader("Almacén");

        addThemeVariants(GridVariant.LUMO_COMPACT,
                GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_WRAP_CELL_CONTENT);


        addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(onSelect));
    }

    private String getCategoryDisplayName(ProductCategory category) {
        if (category == null)
            return "";
        return switch (category) {
            case ALIMENTO -> "Alimento";
            case MEDICINA -> "Medicina";
            case ACCESORIO -> "Accesorio";
            case HIGIENE -> "Higiene";
            case OTRO -> "Otro";
        };
    }
    private Component renderCategoryBadge(Product product) {
        ProductCategory category = product.getCategory();
        if (category == null) {
            return new Span("-");
        }
        com.vaadin.flow.component.html.Span badge = new com.vaadin.flow.component.html.Span(getCategoryDisplayName(category));
        badge.getElement().getThemeList().add("badge pill");
        switch (category) {
            case ALIMENTO -> badge.getElement().getThemeList().add("success");
            case MEDICINA -> badge.getElement().getThemeList().add("primary");
            case ACCESORIO -> badge.getElement().getThemeList().add("contrast");
            case HIGIENE -> badge.getElement().getThemeList().add("warning");
            case OTRO -> badge.getElement().getThemeList().add("error");
        }
        return badge;
    }

    public void setSpecification(Specification<Product> specification) {
        this.specification = specification;
        setItems(query -> {
            var products = productService.getAllProducts(
                    specification,
                    PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query))
            );
            return products.stream().filter(Product::isActive);
        });
    }

    // Add this method to ProductGrid.java
    private Component createActionsColumn(Product product) {
        Button edit = new Button(new Icon(VaadinIcon.EDIT));
        edit.addThemeVariants(
                ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        edit.getElement().setProperty("title", "Editar");
        edit.getStyle().set("min-width", "32px").set("width", "32px").set("padding", "0");

        Button delete = new Button(new Icon(VaadinIcon.TRASH));
        delete.addThemeVariants(
                ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_TERTIARY_INLINE,
                ButtonVariant.LUMO_SMALL,
                ButtonVariant.LUMO_ERROR);
        delete.getElement().setProperty("title", "Eliminar");
        delete.getStyle().set("min-width", "32px").set("width", "32px").set("padding", "0");

        delete.addClickListener(e -> showDeleteConfirmationDialog(product));

        HorizontalLayout actions = new HorizontalLayout(edit, delete);
        actions.setSpacing(true);
        actions.setPadding(false);
        actions.setMargin(false);
        actions.setWidth(null);
        return actions;
    }

    private void showDeleteConfirmationDialog(Product product) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirmar eliminación");
        confirmDialog.setModal(true);
        confirmDialog.setWidth("400px");

        Span message = new Span("¿Está seguro de que desea eliminar el producto \"" +
                product.getName() + "\"? Esta acción no se puede deshacer.");
        message.getStyle().set("margin-bottom", "20px");

        Button confirmButton = new Button("Eliminar");
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirmButton.addClickListener(e -> {
            try {
                deleteProduct(product);
                confirmDialog.close();
            } catch (Exception ex) {
                NotificationUtils.error("Error al eliminar el producto: " + ex.getMessage());
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

    private void deleteProduct(Product product) {
        try {
            // Assuming you have a soft delete method in ProductService
            productService.delete(product.getId());
            NotificationUtils.success("Producto eliminado exitosamente");
            getDataProvider().refreshAll();
        } catch (Exception e) {
            NotificationUtils.error("Error al eliminar producto: " + e.getMessage());
        }
    }
}