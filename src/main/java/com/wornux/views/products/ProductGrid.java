package com.wornux.views.products;

import com.vaadin.flow.component.grid.Grid;
import com.wornux.data.entity.Product;
import com.wornux.data.enums.ProductCategory;
import com.wornux.services.interfaces.ProductService;
import org.springframework.data.domain.PageRequest;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import java.util.function.Consumer;

public class ProductGrid extends Grid<Product> {

    public ProductGrid(ProductService productService,
                       Consumer<Product> onSelect) {
        super(Product.class, false);
        setWidthFull();
        setHeight("450px");

        setItems(query -> {
            var products = productService.getAllProducts(PageRequest.of(query.getPage(), query.getPageSize(),
                    VaadinSpringDataHelpers.toSpringDataSort(query)));
            return products.stream().filter(Product::isActive);
        });

        addColumn(Product::getName).setAutoWidth(true).setHeader("Nombre");
        addColumn(product -> product.getDescription() != null ? product.getDescription() : "")
                .setAutoWidth(true).setHeader("Descripción");
        addColumn(product -> "$" + (product.getPurchasePrice() != null ? product.getPurchasePrice().toString() : "0.00"))
                .setAutoWidth(true).setHeader("Precio de Compra");
        addColumn(product -> "$" + (product.getSalesPrice() != null ? product.getSalesPrice().toString() : "0.00"))
                .setAutoWidth(true).setHeader("Precio de Venta");
        addColumn(Product::getAccountingStock).setAutoWidth(true).setHeader("Stock Contable");
        addColumn(Product::getAvailableStock).setAutoWidth(true).setHeader("Stock Disponible");
        addColumn(product -> getCategoryDisplayName(product.getCategory())).setAutoWidth(true)
                .setHeader("Categoría");
        addColumn(product -> {
            try {
                return product.getSupplier() != null ? product.getSupplier().getCompanyName() : "Sin Proveedor";
            } catch (org.hibernate.LazyInitializationException e) {
                return "N/A";
            }
        }).setAutoWidth(true).setHeader("Proveedor");
        addColumn(product -> product.getWarehouse() != null ? product.getWarehouse().getName() : "Sin Almacén")
                .setAutoWidth(true).setHeader("Almacén");

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
}