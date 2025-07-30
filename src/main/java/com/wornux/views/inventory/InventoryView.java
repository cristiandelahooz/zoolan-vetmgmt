package com.wornux.views.inventory;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.HasClearButton;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.Warehouse;
import com.wornux.dto.request.ProductUpdateRequestDto;
import com.wornux.services.interfaces.WarehouseService;
import com.wornux.data.entity.Product;
import com.wornux.data.entity.Supplier;
import com.wornux.data.enums.ProductCategory;
import com.wornux.services.interfaces.ProductService;
import com.wornux.services.interfaces.SupplierService;
import com.wornux.utils.NotificationUtils;
import com.wornux.views.products.ProductForm;
import com.wornux.views.products.ProductGrid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

import java.math.BigDecimal;

@Slf4j
@PageTitle("Inventario")
@Route(value = "inventory")
@Menu(order = 3, icon = "line-awesome/svg/boxes-solid.svg")
@Uses(Icon.class)
public class InventoryView extends VerticalLayout {

    // Form fields
    private final TextField name = new TextField("Nombre del Producto", "Ingrese el nombre del producto");
    private final TextArea description = new TextArea("Descripción", "Ingrese la descripción del producto");
    private final NumberField purchasePrice = new NumberField("Precio de Compra", "Ingrese el precio de compra");
    private final NumberField salesPrice = new NumberField("Precio de Venta", "Ingrese el precio de venta");
    private final NumberField accountingStock = new NumberField("Stock Contable", "Ingrese el stock contable");
    private final NumberField availableStock = new NumberField("Stock Disponible", "Ingrese el stock disponible");
    private final NumberField reorderLevel = new NumberField("Nivel de Reorden", "Ingrese el nivel mínimo");
    private final ComboBox<ProductCategory> category = new ComboBox<>("Categoría");
    private final ComboBox<Supplier> supplier = new ComboBox<>("Proveedor");
    private final ComboBox<Warehouse> warehouse = new ComboBox<>("Almacén");

    private final Button saveButton = new Button("Guardar", new Icon(VaadinIcon.CHECK_CIRCLE));
    private final Button newButton = new Button("Nuevo", new Icon(VaadinIcon.PLUS_CIRCLE));
    private final Button deleteButton = new Button("Eliminar", new Icon(VaadinIcon.TRASH));

    // Grid and search
    private final ProductGrid productGrid;
    private final TextField searchField = new TextField("Busqueda de Productos");
    private final Button toggleGridButton = new Button("Mostrar Grid", new Icon(VaadinIcon.EYE));
    private final MultiSelectComboBox<ProductCategory> categoryFilter = new MultiSelectComboBox<>("Filtrar por Categoría");
    private final ComboBox<Warehouse> warehouseFilter = new ComboBox<>("Filtrar por Almacén");

    private final Binder<Product> binder = new BeanValidationBinder<>(Product.class);
    private final transient ProductService productService;
    private final transient SupplierService supplierService;
    private final ProductForm productForm;
    private transient Product selectedProduct;
    private final transient WarehouseService warehouseService;

    private static final String RESPONSIVE_STEP_WIDTH = "500px";
    private static final String GRID_HEIGHT = "450px";
    private static final String VALID_NUMBER = "Debe ser un número válido";
    private static final String VALID_PRICE = "Debe ser un precio válido";

    public InventoryView(@Qualifier("productServiceImpl") ProductService productService,
                         @Qualifier("supplierServiceImpl") SupplierService supplierService,
                         @Qualifier("warehouseServiceImpl") WarehouseService warehouseService) {
        this.productService = productService;
        this.supplierService = supplierService;
        this.warehouseService = warehouseService;
        this.productForm = new ProductForm(productService, supplierService, warehouseService);
        this.productGrid = new ProductGrid(productService, product -> {
            selectedProduct = product;
            binder.setBean(selectedProduct);
            deleteButton.setEnabled(true);
        });
        setupComponents();
        setupForm();
        setupEventListeners();
        setupLayout();
    }

    private void setupLayout() {
        productGrid.setVisible(false);
        searchField.setVisible(false);
        categoryFilter.setVisible(false);
        warehouseFilter.setVisible(false);
        deleteButton.setEnabled(false);

        toggleGridButton.setWidthFull();
        searchField.setWidthFull();
        categoryFilter.setWidthFull();
        productGrid.setWidthFull();
        productGrid.setHeight(GRID_HEIGHT);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.setSizeUndefined();

        content.add(toggleGridButton);
        content.add(createGridFilters());
        content.add(productGrid);
        content.add(createFormLayout());
        content.add(createFooterBar());

        Scroller scroller = new Scroller(content);
        scroller.setSizeFull();

        removeAll();
        add(scroller);
        add(createFooterBar());
        setSizeFull();

        addClassNames(LumoUtility.Padding.LARGE);
        setSpacing(true);
    }

    private void setupComponents() {
        toggleGridButton.addClickListener(e -> {
            boolean isVisible = !productGrid.isVisible();
            productGrid.setVisible(isVisible);
            searchField.setVisible(isVisible);
            categoryFilter.setVisible(isVisible);
            warehouseFilter.setVisible(isVisible);
            toggleGridButton.setIcon(new Icon(isVisible ? VaadinIcon.EYE_SLASH : VaadinIcon.EYE));
            toggleGridButton.setText(isVisible ? "Ocultar Grid" : "Mostrar Grid");
            if (isVisible) {
                searchField.focus();
            }
        });

        searchField.setClearButtonVisible(true);
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.setPlaceholder("Buscar por nombre, descripción, categoría...");
        searchField.addValueChangeListener(e -> productGrid.getDataProvider().refreshAll());

        warehouseFilter.setItems(warehouseService.getAllWarehouses().stream()
                .map(dto -> {
                    Warehouse w = new Warehouse();
                    w.setId(dto.getId());
                    w.setName(dto.getName());
                    return w;
                }).toList());
        warehouseFilter.setItemLabelGenerator(Warehouse::getName);
        warehouseFilter.setClearButtonVisible(true);
        warehouseFilter.addValueChangeListener(e -> productGrid.getDataProvider().refreshAll());

        categoryFilter.setItems(ProductCategory.values());
        categoryFilter.setItemLabelGenerator(this::getCategoryDisplayName);
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.setAutoExpand(MultiSelectComboBox.AutoExpandMode.BOTH);
        categoryFilter.addValueChangeListener(e -> productGrid.getDataProvider().refreshAll());
    }

    private void setupForm() {
        configureFormFields();
        configureBinders();
    }

    private void configureFormFields() {
        description.setHeight("80px");

        purchasePrice.setMin(0);
        purchasePrice.setStep(0.01);
        purchasePrice.setStepButtonsVisible(true);

        salesPrice.setMin(0);
        salesPrice.setStep(0.01);
        salesPrice.setStepButtonsVisible(true);

        accountingStock.setMin(0);
        accountingStock.setStep(1);
        accountingStock.setStepButtonsVisible(true);

        availableStock.setMin(0);
        availableStock.setStep(1);
        availableStock.setStepButtonsVisible(true);

        reorderLevel.setMin(0);
        reorderLevel.setStep(1);
        reorderLevel.setStepButtonsVisible(true);
        reorderLevel.setValue(5.0);

        category.setItems(ProductCategory.values());
        category.setItemLabelGenerator(this::getCategoryDisplayName);

        supplier.setItems(supplierService.getAllSuppliers());
        supplier.setItemLabelGenerator(Supplier::getCompanyName);

        warehouse.setItems(warehouseService.getAllWarehouses().stream()
                .map(dto -> {
                    Warehouse w = new Warehouse();
                    w.setId(dto.getId());
                    w.setName(dto.getName());
                    return w;
                }).toList());
        warehouse.setItemLabelGenerator(Warehouse::getName);
    }

    private void configureBinders() {
        binder.forField(accountingStock).withConverter(Double::intValue, Integer::doubleValue, VALID_NUMBER)
                .bind(Product::getAccountingStock, Product::setAccountingStock);

        binder.forField(availableStock).withConverter(Double::intValue, Integer::doubleValue, VALID_NUMBER)
                .bind(Product::getAvailableStock, Product::setAvailableStock);

        binder.forField(reorderLevel).withConverter(Double::intValue, Integer::doubleValue, VALID_NUMBER)
                .bind(Product::getReorderLevel, Product::setReorderLevel);

        binder.forField(name).bind(Product::getName, Product::setName);
        binder.forField(description).bind(Product::getDescription, Product::setDescription);

        binder.forField(purchasePrice).withConverter(BigDecimal::valueOf, Number::doubleValue, VALID_PRICE)
                .bind(Product::getPurchasePrice, Product::setPurchasePrice);

        binder.forField(salesPrice).withConverter(BigDecimal::valueOf, Number::doubleValue, VALID_PRICE)
                .bind(Product::getSalesPrice, Product::setSalesPrice);

        binder.forField(category).bind(Product::getCategory, Product::setCategory);
        binder.forField(supplier).bind(Product::getSupplier, Product::setSupplier);
        binder.forField(warehouse).bind(Product::getWarehouse, Product::setWarehouse);

        binder.getFields().forEach(field -> {
            if (field instanceof HasClearButton clear) {
                clear.setClearButtonVisible(true);
            }
        });
    }

    private void setupEventListeners() {
        newButton.addClickListener(e ->
                productForm.openForNew()
        );

        productGrid.addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(productForm::openForEdit));

        productForm.addProductSavedListener(product -> {
            refreshAll();
            NotificationUtils.success("Producto guardado/actualizado exitosamente");
        });

        productForm.addProductCancelledListener(() -> {
            selectedProduct = null;
            binder.setBean(null);
            deleteButton.setEnabled(false);
            productGrid.deselectAll();
        });

        deleteButton.addClickListener(this::delete);
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

    private HorizontalLayout createGridFilters() {
        searchField.setWidth("100%");
        categoryFilter.setWidth("100%");
        warehouseFilter.setWidth("100%");

        HorizontalLayout filters = new HorizontalLayout(searchField, categoryFilter, warehouseFilter);
        filters.setWidthFull();
        filters.setFlexGrow(1, searchField);
        filters.setFlexGrow(1, warehouseFilter);
        filters.setFlexGrow(1, categoryFilter);
        filters.addClassNames(LumoUtility.Gap.MEDIUM);
        return filters;
    }

    private HorizontalLayout createFormLayout() {
        H3 headerProduct = new H3("Información del Producto");
        H3 headerStock = new H3("Inventario");
        headerStock.addClassNames(LumoUtility.Margin.Top.LARGE);

        FormLayout formLayout = new FormLayout();
        formLayout.addClassNames(LumoUtility.Padding.Right.XLARGE);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep(RESPONSIVE_STEP_WIDTH, 2));

        HorizontalLayout stockLayout = new HorizontalLayout();
        stockLayout.setPadding(false);
        stockLayout.setMargin(false);
        stockLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        stockLayout.addClassNames(LumoUtility.FlexWrap.WRAP);
        stockLayout.add(accountingStock, availableStock, reorderLevel);
        stockLayout.setFlexGrow(1, accountingStock, availableStock, reorderLevel);

        formLayout.add(headerProduct, name, description, purchasePrice, salesPrice, category, supplier, warehouse, headerStock, stockLayout);
        formLayout.setColspan(headerProduct, 2);
        formLayout.setColspan(description, 2);
        formLayout.setColspan(headerStock, 2);
        formLayout.setColspan(stockLayout, 2);

        HorizontalLayout horizontalLayout = new HorizontalLayout(formLayout);
        horizontalLayout.addClassNames(LumoUtility.Padding.Top.SMALL);
        horizontalLayout.setWidthFull();

        return horizontalLayout;
    }

    private void delete(ClickEvent<Button> buttonClickEvent) {
        try {
            if (selectedProduct != null) {
                productService.delete(selectedProduct.getId());
                NotificationUtils.success("Producto eliminado exitosamente");

                selectedProduct = null;
                binder.setBean(null);
                deleteButton.setEnabled(false);
                refreshAll();
            }
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage());
            NotificationUtils.error("Error al eliminar el producto: " + ex.getMessage());
        }
    }

    private void refreshAll() {
        productGrid.getDataProvider().refreshAll();
    }

    private HorizontalLayout createFooterBar() {
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, newButton, deleteButton);
        buttonLayout.addClassNames(LumoUtility.FlexWrap.WRAP, LumoUtility.Padding.MEDIUM);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setWidthFull();
        return buttonLayout;
    }
}