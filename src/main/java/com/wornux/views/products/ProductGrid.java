package com.wornux.views.products;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.wornux.data.entity.Product;
import com.wornux.data.enums.ProductCategory;
import com.wornux.services.interfaces.ProductService;
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
}