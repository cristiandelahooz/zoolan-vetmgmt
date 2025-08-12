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
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.components.Breadcrumb;
import com.wornux.components.BreadcrumbItem;
import com.wornux.components.InfoIcon;
import com.wornux.data.entity.Product;
import com.wornux.data.entity.Warehouse;
import com.wornux.data.enums.ProductCategory;
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
    private final Button newButton = new Button("Nuevo Producto");

    private final transient ProductService productService;
    private final transient WarehouseService warehouseService;
    private final transient ProductForm productForm;

    public InventoryView(@Qualifier("productServiceImpl") ProductService productService, @Qualifier("supplierServiceImpl") SupplierService supplierService, @Qualifier("warehouseServiceImpl") WarehouseService warehouseService) {
        this.productService = productService;
        this.warehouseService = warehouseService;
        this.productForm = new ProductForm(productService, supplierService, warehouseService);
        this.productGrid = new ProductGrid(productService, productForm::openForEdit);

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
        searchField.setClearButtonVisible(true);
        searchField.setPlaceholder("Buscar por nombre, descripción, proveedor...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> refreshGrid());

        categoryFilter.setItems(ProductCategory.values());
        categoryFilter.setItemLabelGenerator(this::getCategoryDisplayName);
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.setAutoExpand(MultiSelectComboBox.AutoExpandMode.BOTH);
        categoryFilter.addValueChangeListener(e -> refreshGrid());


        warehouseFilter.setItems(warehouseService.getAllWarehouses().stream().map(dto -> {
            Warehouse w = new Warehouse();
            w.setId(dto.getId());
            w.setName(dto.getName());
            return w;
        }).toList());
        warehouseFilter.setItemLabelGenerator(Warehouse::getName);
        warehouseFilter.setClearButtonVisible(true);
        warehouseFilter.addValueChangeListener(e -> refreshGrid());

        HorizontalLayout toolbar = new HorizontalLayout(searchField, categoryFilter, warehouseFilter);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        return toolbar;
    }

    public Specification<Product> createProductFilterSpecification() {
        return (root, query, builder) -> {
            Order order = builder.asc(root.get("name"));
            if (query != null) {
                query.orderBy(order);
            }

            String search = searchField.getValue() != null ? searchField.getValue().trim().toLowerCase() : "";
            var categoryItems = categoryFilter.getSelectedItems();
            Warehouse selectedWarehouse = warehouseFilter.getValue();

            Predicate searchPredicate = builder.conjunction();
            if (!search.isEmpty()) {
                searchPredicate = builder.or(builder.like(builder.lower(root.get("name")), "%" + search + "%"), builder.like(builder.lower(root.get("description")), "%" + search + "%"), builder.like(builder.lower(root.get("supplier").get("companyName")), "%" + search + "%"), builder.like(builder.lower(root.get("warehouse").get("name")), "%" + search + "%"));
            }

            Predicate categoryPredicate = builder.conjunction();
            if (categoryItems != null && !categoryItems.isEmpty()) {
                categoryPredicate = root.get("category").in(categoryItems);
            }

            Predicate warehousePredicate = builder.conjunction();
            if (selectedWarehouse != null) {
                warehousePredicate = builder.equal(root.get("warehouse").get("id"), selectedWarehouse.getId());
            }

            Predicate activePredicate = builder.equal(root.get("active"), true);

            return builder.and(searchPredicate, categoryPredicate, warehousePredicate, activePredicate);
        };
    }

    private void refreshGrid() {
        productGrid.setSpecification(createProductFilterSpecification());
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
}