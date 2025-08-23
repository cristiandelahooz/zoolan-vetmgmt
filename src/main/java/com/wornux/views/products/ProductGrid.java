package com.wornux.views.products;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.wornux.data.entity.Product;
import com.wornux.data.entity.Warehouse;
import com.wornux.data.enums.ProductCategory;
import com.wornux.data.enums.ProductUnit;
import com.wornux.data.enums.ProductUsageType;
import com.wornux.services.interfaces.ProductService;
import com.wornux.services.interfaces.WarehouseService;
import com.wornux.utils.NotificationUtils;
import java.util.function.Consumer;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

public class ProductGrid extends Grid<Product> {
  private static final String COLUMN_WIDTH = "120px";
  private final transient ProductService productService;
  private final transient WarehouseService warehouseService;
  private Specification<Product> specification;
  // Filtros
  private ComboBox<ProductUnit> unitFilter;
  private ComboBox<ProductUsageType> usageTypeFilter;
  private ComboBox<Warehouse> warehouseFilter;

  public ProductGrid(
      ProductService productService,
      WarehouseService warehouseService,
      Consumer<Product> onSelect) {
    super(Product.class, false);
    this.productService = productService;
    this.warehouseService = warehouseService;

    setupGrid();
    setupColumns();
    addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(onSelect));
  }

  private void setupGrid() {
    setItems(
        query -> {
          var products =
              productService.getAllProducts(
                  PageRequest.of(
                      query.getPage(),
                      query.getPageSize(),
                      VaadinSpringDataHelpers.toSpringDataSort(query)));
          return products.stream().filter(Product::isActive);
        });

    addThemeVariants(
        GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
  }

  private void setupColumns() {
    addColumn(Product::getName).setWidth("150px").setHeader("Nombre");
    addColumn(product -> product.getDescription() != null ? product.getDescription() : "")
        .setWidth("200px")
        .setHeader("Descripción");
    addColumn(
            product ->
                "$"
                    + (product.getPurchasePrice() != null
                        ? product.getPurchasePrice().toString()
                        : "0.00"))
        .setWidth(COLUMN_WIDTH)
        .setHeader("Precio de Compra");
    addColumn(
            product ->
                "$"
                    + (product.getSalesPrice() != null
                        ? product.getSalesPrice().toString()
                        : "0.00"))
        .setWidth(COLUMN_WIDTH)
        .setHeader("Precio de Venta");

    addColumn(Product::getFormattedStock).setWidth(COLUMN_WIDTH).setHeader("Stock Contable");
    addColumn(Product::getFormattedAvailableStock)
        .setWidth(COLUMN_WIDTH)
        .setHeader("Stock Disponible");

    addComponentColumn(this::renderUnitBadge)
        .setHeader("Unidad")
        .setKey("unitBadge")
        .setWidth(COLUMN_WIDTH);

    addComponentColumn(this::renderUsageTypeBadge)
        .setHeader("Tipo de Uso")
        .setKey("usageTypeBadge")
        .setWidth("130px");

    addComponentColumn(this::renderCategoryBadge)
        .setHeader("Categoría")
        .setKey("categoryBadge")
        .setWidth(COLUMN_WIDTH);

    addColumn(
            product -> {
              try {
                return product.getSupplier() != null
                    ? product.getSupplier().getCompanyName()
                    : "Sin Proveedor";
              } catch (org.hibernate.LazyInitializationException e) {
                return "N/A";
              }
            })
        .setAutoWidth(true)
        .setHeader("Proveedor");

    addColumn(
            product ->
                product.getWarehouse() != null ? product.getWarehouse().getName() : "Sin Almacén")
        .setWidth(COLUMN_WIDTH)
        .setHeader("Almacén");
  }

  private Component renderUnitBadge(Product product) {
    ProductUnit unit = product.getUnit();
    if (unit == null) {
      return new Span("-");
    }

    Span badge = new Span(unit.getDisplayName());
    badge.getElement().getThemeList().add("badge pill");
    badge.getElement().getThemeList().add("contrast");
    return badge;
  }

  private Component renderUsageTypeBadge(Product product) {
    ProductUsageType usageType = product.getUsageType();
    if (usageType == null) {
      return new Span("-");
    }

    Span badge = new Span(usageType.getDisplayName());
    badge.getElement().getThemeList().add("badge pill");

    switch (usageType) {
      case PRIVADO -> badge.getElement().getThemeList().add("warning");
      case VENTA -> badge.getElement().getThemeList().add("success");
      case AMBOS -> badge.getElement().getThemeList().add("primary");
    }

    return badge;
  }

  private String getCategoryDisplayName(ProductCategory category) {
    if (category == null) return "";
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

    Span badge = new Span(getCategoryDisplayName(category));
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

  public Component createFilters() {
    unitFilter = new ComboBox<>("Filtrar por Unidad");
    unitFilter.setItems(ProductUnit.values());
    unitFilter.setItemLabelGenerator(ProductUnit::getDisplayName);
    unitFilter.setClearButtonVisible(true);
    unitFilter.addValueChangeListener(e -> applyFilters());

    usageTypeFilter = new ComboBox<>("Filtrar por Tipo de Uso");
    usageTypeFilter.setItems(ProductUsageType.values());
    usageTypeFilter.setItemLabelGenerator(ProductUsageType::getDisplayName);
    usageTypeFilter.setClearButtonVisible(true);
    usageTypeFilter.addValueChangeListener(e -> applyFilters());

    warehouseFilter = new ComboBox<>("Filtrar por Almacén");
    // Cargar warehouses
    try {
      var warehouses =
          warehouseService.getAllWarehouses().stream()
              .map(
                  dto -> {
                    Warehouse w = new Warehouse();
                    w.setId(dto.getId());
                    w.setName(dto.getName());
                    return w;
                  })
              .toList();
      warehouseFilter.setItems(warehouses);
      warehouseFilter.setItemLabelGenerator(Warehouse::getName);
    } catch (Exception e) {
      // Handle if warehouse offering is not available
    }
    warehouseFilter.setClearButtonVisible(true);
    warehouseFilter.addValueChangeListener(e -> applyFilters());

    Button clearFilters = new Button("Limpiar Filtros", VaadinIcon.REFRESH.create());
    clearFilters.addClickListener(e -> clearAllFilters());

    HorizontalLayout filterLayout =
        new HorizontalLayout(unitFilter, usageTypeFilter, warehouseFilter, clearFilters);
    filterLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
    filterLayout.setSpacing(true);

    return filterLayout;
  }

  private void applyFilters() {
    setItems(
        query -> {
          // Usar el repositorio con filtros si están disponibles
          ProductUnit selectedUnit = unitFilter.getValue();
          ProductUsageType selectedUsageType = usageTypeFilter.getValue();
          Long selectedWarehouseId =
              warehouseFilter.getValue() != null ? warehouseFilter.getValue().getId() : null;

          // Si no hay filtros, usar consulta normal
          if (selectedUnit == null && selectedUsageType == null && selectedWarehouseId == null) {
            var products =
                productService.getAllProducts(
                    PageRequest.of(
                        query.getPage(),
                        query.getPageSize(),
                        VaadinSpringDataHelpers.toSpringDataSort(query)));
            return products.stream().filter(Product::isActive);
          }

          // Aplicar filtros usando el repositorio
          try {
            var products =
                productService
                    .getProductRepository()
                    .findWithFilters(
                        selectedUnit,
                        selectedUsageType,
                        selectedWarehouseId,
                        PageRequest.of(
                            query.getPage(),
                            query.getPageSize(),
                            VaadinSpringDataHelpers.toSpringDataSort(query)));
            return products.stream();
          } catch (Exception e) {
            // Fallback to normal query
            var products =
                productService.getAllProducts(
                    PageRequest.of(
                        query.getPage(),
                        query.getPageSize(),
                        VaadinSpringDataHelpers.toSpringDataSort(query)));
            return products.stream().filter(Product::isActive);
          }
        });
  }

  private void clearAllFilters() {
    unitFilter.clear();
    usageTypeFilter.clear();
    warehouseFilter.clear();
    applyFilters();
  }

  public void setSpecification(Specification<Product> specification) {
    this.specification = specification;
    setItems(
        query -> {
          var products =
              productService.getAllProducts(
                  specification,
                  PageRequest.of(
                      query.getPage(),
                      query.getPageSize(),
                      VaadinSpringDataHelpers.toSpringDataSort(query)));
          return products.stream().filter(Product::isActive);
        });
  }

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

    Span message =
        new Span(
            "¿Está seguro de que desea eliminar el producto \""
                + product.getName()
                + "\"? Esta acción no se puede deshacer.");
    message.getStyle().set("margin-bottom", "20px");

    Button confirmButton = new Button("Eliminar");
    confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
    confirmButton.addClickListener(
        e -> {
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
      productService.delete(product.getId());
      NotificationUtils.success("Producto eliminado exitosamente");
      getDataProvider().refreshAll();
    } catch (Exception e) {
      NotificationUtils.error("Error al eliminar producto: " + e.getMessage());
    }
  }
}
