package com.wornux.views.inventory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.components.Breadcrumb;
import com.wornux.components.BreadcrumbItem;
import com.wornux.components.InfoIcon;
import com.wornux.data.entity.Product;
import com.wornux.data.entity.Warehouse;
import com.wornux.data.enums.ProductCategory;
import com.wornux.data.enums.ProductUnit;
import com.wornux.data.enums.ProductUsageType;
import com.wornux.services.interfaces.ProductService;
import com.wornux.services.interfaces.SupplierService;
import com.wornux.services.interfaces.WarehouseService;
import com.wornux.utils.NotificationUtils;
import com.wornux.views.consultations.ConsultationsView;
import com.wornux.views.products.ProductForm;
import com.wornux.views.products.ProductGrid;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;


@Slf4j
@PageTitle("Inventario")
@Route(value = "inventario")
@Menu(order = 3, icon = "line-awesome/svg/boxes-solid.svg")
public class InventoryView extends Div {

  private final ProductGrid productGrid;
  private final TextField searchField = new TextField("Buscar productos");
  private final MultiSelectComboBox<ProductCategory> categoryFilter = new MultiSelectComboBox<>("Filtrar por Categoría");
  private final ComboBox<Warehouse> warehouseFilter = new ComboBox<>("Filtrar por Almacén");
  private ComboBox<ProductUnit> unitFilter;
  private ComboBox<ProductUsageType> usageTypeFilter;
  private final Button newButton = new Button("Nuevo Producto");
  private final Span quantity = new Span();

  private final transient ProductService productService;
  private final transient WarehouseService warehouseService;
  private final transient ProductForm productForm;

  public InventoryView(@Qualifier("productServiceImpl") ProductService productService, @Qualifier("supplierServiceImpl") SupplierService supplierService, @Qualifier("warehouseServiceImpl") WarehouseService warehouseService) {
    this.productService = productService;
    this.warehouseService = warehouseService;
    this.productForm = new ProductForm(productService, supplierService, warehouseService);
    this.productGrid = new ProductGrid(productService, warehouseService, productForm::openForEdit);

    setId("inventory-view");
    setSizeFull();

    productForm.setOnSaveCallback(() -> {
      refreshGrid();
      productForm.close();
    });

    createGrid(createProductFilterSpecification());

    final Div gridLayout = new Div(productGrid);
    gridLayout.setHeightFull();
    gridLayout.setWidth("99%");
    gridLayout.addClassNames(
        LumoUtility.Margin.Bottom.LARGE,
        LumoUtility.Overflow.HIDDEN
    );

    productGrid.setWidthFull();
    productGrid.setHeightFull();

    add(createTitle(), createFilter(), gridLayout);

    addClassNames(
        LumoUtility.Margin.Horizontal.SMALL,
        LumoUtility.Display.FLEX,
        LumoUtility.FlexDirection.COLUMN,
        LumoUtility.Height.FULL,
        LumoUtility.Overflow.HIDDEN
    );
    newButton.addClickListener(event -> productForm.openForNew());
  }

  private void createGrid(Specification<Product> specification) {
    productGrid.setSpecification(specification);
    var actionsColumn = productGrid.addComponentColumn(this::createActionsColumn).setHeader("Acciones").setAutoWidth(true);
    actionsColumn.setFrozenToEnd(true);
  }

  private Div createTitle() {
    final Breadcrumb breadcrumb = new Breadcrumb();
    breadcrumb.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
    breadcrumb.add(new BreadcrumbItem("Inventario", InventoryView.class), new BreadcrumbItem("Lista de Productos", ConsultationsView.class));


    Icon icon = InfoIcon.INFO_CIRCLE.create("Gestionar productos de la clínica veterinaria.");

    Div headerLayout = new Div(breadcrumb, icon);
    headerLayout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.ROW, LumoUtility.Margin.Top.SMALL);

    newButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);
    newButton.addClassNames(LumoUtility.Width.AUTO);

    Div layout = new Div(headerLayout, newButton);
    layout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.FlexDirection.Breakpoint.Large.ROW, LumoUtility.JustifyContent.BETWEEN, LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Margin.Top.SMALL, LumoUtility.Gap.XSMALL, LumoUtility.AlignItems.STRETCH, LumoUtility.AlignItems.Breakpoint.Large.END);

    return layout;
  }

  private Component createFilter() {
    searchField.setPlaceholder("Buscar por nombre...");
    searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
    searchField.setValueChangeMode(ValueChangeMode.LAZY);
    searchField.setClearButtonVisible(true);
    searchField.addValueChangeListener(e -> updateList());
    searchField.setWidth("40%");

    // Configurar filtro de categorías
    categoryFilter.setItems(ProductCategory.values());
    categoryFilter.setItemLabelGenerator(this::getCategoryDisplayName);
    categoryFilter.setClearButtonVisible(true);
    categoryFilter.addSelectionListener(e -> updateList());
    categoryFilter.setWidth("30%");


    // Configurar filtro de almacenes
    try {
      var warehouses = warehouseService.getAllWarehouses().stream()
          .map(dto -> {
            Warehouse w = new Warehouse();
            w.setId(dto.getId());
            w.setName(dto.getName());
            return w;
          }).toList();
      warehouseFilter.setItems(warehouses);
      warehouseFilter.setItemLabelGenerator(Warehouse::getName);
    } catch (Exception e) {
      log.warn("Error loading warehouses for filter: {}", e.getMessage());
    }
    warehouseFilter.setClearButtonVisible(true);
    warehouseFilter.addValueChangeListener(e -> updateList());
    warehouseFilter.setWidth("30%");
    warehouseFilter.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM);

    // Inicializar y configurar filtro de unidades
    unitFilter = new ComboBox<>("Filtrar por Unidad");
    unitFilter.setItems(ProductUnit.values());
    unitFilter.setItemLabelGenerator(ProductUnit::getDisplayName);
    unitFilter.setClearButtonVisible(true);
    unitFilter.addValueChangeListener(e -> updateList());
    unitFilter.setWidth("30%");
    unitFilter.addClassNames(LumoUtility.Margin.Horizontal.XSMALL);

    // Inicializar y configurar filtro de tipo de uso
    usageTypeFilter = new ComboBox<>("Filtrar por Tipo de Uso");
    usageTypeFilter.setItems(ProductUsageType.values());
    usageTypeFilter.setItemLabelGenerator(ProductUsageType::getDisplayName);
    usageTypeFilter.setClearButtonVisible(true);
    usageTypeFilter.addValueChangeListener(e -> updateList());
    usageTypeFilter.setWidth("30%");


    quantity.addClassNames(LumoUtility.BorderRadius.SMALL, LumoUtility.Height.XSMALL, LumoUtility.FontWeight.MEDIUM,
        LumoUtility.JustifyContent.CENTER, LumoUtility.AlignItems.CENTER, LumoUtility.Padding.XSMALL,
        LumoUtility.Padding.Horizontal.SMALL, LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Margin.Bottom.XSMALL,
        LumoUtility.TextColor.PRIMARY_CONTRAST, LumoUtility.Background.PRIMARY);
    updateQuantity();

    // Botón para limpiar todos los filtros
    Button clearFilters = new Button("Limpiar Filtros", VaadinIcon.REFRESH.create());
    clearFilters.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    clearFilters.addClickListener(e -> clearAllFilters());

    // Primera fila de filtros
    HorizontalLayout firstRowFilters = new HorizontalLayout(
        searchField, categoryFilter, warehouseFilter);
    firstRowFilters.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
    firstRowFilters.setWidthFull();
    firstRowFilters.setSpacing(true);

    // Segunda fila de filtros
    HorizontalLayout secondRowFilters = new HorizontalLayout(
        unitFilter, usageTypeFilter, clearFilters, quantity);
    secondRowFilters.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
    quantity.getStyle().set("margin-left", "auto");
    secondRowFilters.setWidthFull();
    secondRowFilters.setSpacing(true);

    VerticalLayout filterLayout = new VerticalLayout(firstRowFilters, secondRowFilters);
    filterLayout.setPadding(false);
    filterLayout.setSpacing(true);

    return filterLayout;
  }

  private void clearAllFilters() {
    searchField.clear();
    categoryFilter.clear();
    warehouseFilter.clear();
    unitFilter.clear();
    usageTypeFilter.clear();
    updateList();
    updateQuantity();
  }


  public Specification<Product> createProductFilterSpecification() {
    return (root, query, criteriaBuilder) -> {
      var predicates = new java.util.ArrayList<Predicate>();

      // Filtro por activo
      predicates.add(criteriaBuilder.isTrue(root.get("active")));

      // Filtro por nombre
      if (searchField.getValue() != null && !searchField.getValue().trim().isEmpty()) {
        predicates.add(criteriaBuilder.like(
            criteriaBuilder.lower(root.get("name")),
            "%" + searchField.getValue().toLowerCase() + "%"));
      }

      // Filtro por categorías
      if (categoryFilter.getValue() != null && !categoryFilter.getValue().isEmpty()) {
        predicates.add(root.get("category").in(categoryFilter.getValue()));
      }

      // Filtro por almacén
      if (warehouseFilter.getValue() != null) {
        predicates.add(criteriaBuilder.equal(
            root.get("warehouse").get("id"),
            warehouseFilter.getValue().getId()));
      }

      // Filtro por unidad
      if (unitFilter.getValue() != null) {
        predicates.add(criteriaBuilder.equal(
            root.get("unit"),
            unitFilter.getValue()));
      }

      // Filtro por tipo de uso
      if (usageTypeFilter.getValue() != null) {
        predicates.add(criteriaBuilder.equal(
            root.get("usageType"),
            usageTypeFilter.getValue()));
      }

      // Ordenar por nombre
      query.orderBy(criteriaBuilder.asc(root.get("name")));

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }

  private void updateList() {
    try {
      var specification = createProductFilterSpecification();
      productGrid.setSpecification(specification);

      // Actualizar contador
      long count = productService.getCount(specification);
      quantity.setText(count + " producto" + (count != 1 ? "s" : ""));

    } catch (Exception e) {
      log.error("Error updating product list", e);
      NotificationUtils.error("Error al actualizar la lista de productos");
    }
  }

  private void refreshGrid() {
    productGrid.setSpecification(createProductFilterSpecification());
    updateQuantity();
    productGrid.getDataProvider().refreshAll();
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

    edit.addClickListener(e -> productForm.openForEdit(product));
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
        (product.getName() != null ? product.getName() : "") +
        "\"? Esta acción no se puede deshacer.");
    message.getStyle().set("margin-bottom", "20px");

    Button confirmButton = new Button("Eliminar");
    confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
    confirmButton.addClickListener(e -> {
      try {
        productService.delete(product.getId());
        NotificationUtils.success("Producto eliminado exitosamente");
        refreshGrid();
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

  private void updateQuantity() {
    try {
      long count = productService.getAllProducts().stream().filter(Product::isActive).count();
      quantity.setText("Productos (" + count + ")");
    } catch (Exception e) {
      log.warn("Error getting products count", e);
      quantity.setText("Productos (0)");
    }
  }

  private void applyFilters() {
    productGrid.setItems(query -> {
      // Usar el repositorio con filtros si están disponibles
      ProductUnit selectedUnit = unitFilter.getValue();
      ProductUsageType selectedUsageType = usageTypeFilter.getValue();
      Long selectedWarehouseId = warehouseFilter.getValue() != null ?
          warehouseFilter.getValue().getId() : null;

      // Si no hay filtros, usar consulta normal
      if (selectedUnit == null && selectedUsageType == null && selectedWarehouseId == null) {
        var products = productService.getAllProducts(PageRequest.of(query.getPage(),
            query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)));
        return products.stream().filter(Product::isActive);
      }

      // Aplicar filtros usando el repositorio
      try {
        var products = productService.getProductRepository()
            .findWithFilters(selectedUnit, selectedUsageType, selectedWarehouseId,
                PageRequest.of(query.getPage(), query.getPageSize(),
                    VaadinSpringDataHelpers.toSpringDataSort(query)));
        return products.stream();
      } catch (Exception e) {
        // Fallback to normal query
        var products = productService.getAllProducts(PageRequest.of(query.getPage(),
            query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)));
        return products.stream().filter(Product::isActive);
      }
    });
  }
}