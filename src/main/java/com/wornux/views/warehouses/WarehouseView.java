package com.wornux.views.warehouses;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.Warehouse;
import com.wornux.data.enums.WarehouseType;
import com.wornux.dto.request.WarehouseCreateRequestDto;
import com.wornux.dto.request.WarehouseUpdateRequestDto;
import com.wornux.services.interfaces.ProductService;
import com.wornux.services.interfaces.WarehouseService;
import com.wornux.utils.NotificationUtils;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

@Slf4j
@PageTitle("Warehouses")
@Route(value = "warehouses")
@Menu(order = 4, icon = "line-awesome/svg/warehouse-solid.svg")
@RolesAllowed({ "ROLE_SYSTEM_ADMIN", "ROLE_MANAGER", "ROLE_USER" })
public class WarehouseView extends VerticalLayout {

    private final WarehouseGrid warehouseGrid;
    private final Button saveButton = new Button("Guardar", new Icon(VaadinIcon.CHECK_CIRCLE));
    private final Button cancelButton = new Button("Cancelar", new Icon(VaadinIcon.CLOSE_CIRCLE));
    private final Button deleteButton = new Button("Eliminar", new Icon(VaadinIcon.TRASH));
    private final Button toggleGridButton = new Button("Ocultar Grid", new Icon(VaadinIcon.EYE));
    private final TextField searchField = new TextField("Buscar Almacén");
    private final ComboBox<String> statusFilter = new ComboBox<>("Estado");
    private final Span quantity = new Span();

    // Form fields
    private final TextField name = new TextField("Nombre del Almacén");
    private final ComboBox<WarehouseType> warehouseType = new ComboBox<>("Tipo de Almacén");
    private final ComboBox<Boolean> availableForSale = new ComboBox<>("Disponible para Venta");
    private final ComboBox<Boolean> status = new ComboBox<>("Estado");
    private final ProductService productService;

    private transient Warehouse selectedWarehouse;
    private final transient WarehouseService warehouseService;
    private boolean isEditMode = false;
    private ListDataProvider<Warehouse> warehouseDataProvider;

    private final HorizontalLayout gridFilters;
    private final VerticalLayout contentLayout;

    public WarehouseView(@Qualifier("warehouseServiceImpl") WarehouseService warehouseService,
            ProductService productService) {
        this.warehouseService = warehouseService;
        var warehouses = warehouseService.getAllWarehouses();
        warehouseDataProvider = new ListDataProvider<>(warehouses);
        this.warehouseGrid = new WarehouseGrid(warehouseDataProvider, this::editWarehouse, this::deleteWarehouse);

        gridFilters = createGridFilters();
        contentLayout = new VerticalLayout();

        setupComponents();
        setupEventListeners();
        setupLayout();
        this.productService = productService;
    }

    private void setupLayout() {
        setSizeFull();
        addClassNames(LumoUtility.Padding.LARGE);
        setSpacing(true);

        warehouseGrid.setWidthFull();
        warehouseGrid.setHeight("480px");
        warehouseGrid.addClassNames(LumoUtility.Margin.Bottom.SMALL);

        warehouseGrid.setVisible(true);
        gridFilters.setVisible(true);

        contentLayout.setPadding(false);
        contentLayout.setSpacing(false);
        contentLayout.setSizeFull();

        contentLayout.add(toggleGridButton, gridFilters, warehouseGrid, createFormLayout(), createFooterBar());

        removeAll();
        add(contentLayout);
        addClassNames(LumoUtility.Margin.Horizontal.SMALL, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Height.FULL, LumoUtility.Overflow.HIDDEN);
    }

    private void setupComponents() {
        toggleGridButton.setWidthFull();
        toggleGridButton.addClickListener(e -> {
            boolean isVisible = !warehouseGrid.isVisible();
            warehouseGrid.setVisible(isVisible);
            gridFilters.setVisible(isVisible);
            toggleGridButton.setIcon(new Icon(isVisible ? VaadinIcon.EYE_SLASH : VaadinIcon.EYE));
            toggleGridButton.setText(isVisible ? "Ocultar Grid" : "Mostrar Grid");
            if (isVisible) {
                searchField.focus();
            }
        });

        cancelButton.setEnabled(false);

        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setPlaceholder("Buscar por nombre o tipo...");
        searchField.addValueChangeListener(e -> applyFilters());

        statusFilter.setItems("Todos", "Activo", "Inactivo");
        statusFilter.setValue("Todos");
        statusFilter.addValueChangeListener(e -> applyFilters());

        warehouseType.setItems(WarehouseType.values());
        warehouseType.setRequired(true);
        warehouseType.setRequiredIndicatorVisible(true);

        availableForSale.setItems(true, false);
        availableForSale.setItemLabelGenerator(val -> val ? "Si" : "No");
        availableForSale.setRequired(true);
        availableForSale.setRequiredIndicatorVisible(true);

        status.setItems(true, false);
        status.setItemLabelGenerator(val -> val ? "Activo" : "Inactivo");
        status.setRequired(true);
        status.setRequiredIndicatorVisible(true);

        name.setRequired(true);
        name.setRequiredIndicatorVisible(true);
    }

    private void setupEventListeners() {
        cancelButton.addClickListener(e -> {
            clearForm();
            isEditMode = false;
            selectedWarehouse = null;
            updateCancelButtonState();
        });

        saveButton.addClickListener(e -> saveOrUpdateWarehouse());

        name.addValueChangeListener(e -> validateForm());
        warehouseType.addValueChangeListener(e -> validateForm());
        availableForSale.addValueChangeListener(e -> validateForm());
        status.addValueChangeListener(e -> validateForm());

        name.addValueChangeListener(e -> updateCancelButtonState());
        warehouseType.addValueChangeListener(e -> updateCancelButtonState());
        availableForSale.addValueChangeListener(e -> updateCancelButtonState());
        status.addValueChangeListener(e -> updateCancelButtonState());
    }

    private void updateCancelButtonState() {
        boolean hasContent = !name.isEmpty() || !warehouseType.isEmpty() || !availableForSale.isEmpty() || !status
                .isEmpty();
        cancelButton.setEnabled(hasContent);
    }

    private HorizontalLayout createGridFilters() {
        searchField.setWidth("50%");
        statusFilter.setWidth("45%");
        quantity.addClassNames(LumoUtility.BorderRadius.SMALL, LumoUtility.Height.XSMALL, LumoUtility.FontWeight.MEDIUM,
                LumoUtility.JustifyContent.CENTER, LumoUtility.AlignItems.CENTER, LumoUtility.Padding.XSMALL,
                LumoUtility.Padding.Horizontal.SMALL, LumoUtility.Margin.Horizontal.SMALL,
                LumoUtility.Margin.Bottom.XSMALL, LumoUtility.TextColor.PRIMARY_CONTRAST,
                LumoUtility.Background.PRIMARY);
        quantity.setWidth("15%");
        updateQuantity();

        HorizontalLayout filters = new HorizontalLayout(searchField, statusFilter, quantity);
        filters.setWidthFull();
        filters.setFlexGrow(1, searchField, statusFilter, quantity);
        filters.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        filters.setAlignItems(FlexComponent.Alignment.END);
        filters.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Margin.Top.SMALL,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.MEDIUM, LumoUtility.Width.FULL);
        return filters;
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.addClassNames(LumoUtility.Padding.Right.XLARGE);
        formLayout.setWidth("500px");
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

        formLayout.add(new H3("Información del Almacén"), name, warehouseType, availableForSale, status);
        formLayout.setColspan(name, 2);
        formLayout.setColspan(warehouseType, 2);
        formLayout.setColspan(availableForSale, 2);
        formLayout.setColspan(status, 2);

        return formLayout;
    }

    private HorizontalLayout createFooterBar() {
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.addClassNames(LumoUtility.FlexWrap.WRAP, LumoUtility.Padding.MEDIUM);
        buttonLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
        buttonLayout.setWidthFull();
        return buttonLayout;
    }

    private void saveOrUpdateWarehouse() {
        if (!validateForm()) {
            NotificationUtils.error("Por favor, complete todos los campos requeridos");
            return;
        }
        if (isEditMode && selectedWarehouse != null) {
            updateWarehouse();
        } else {
            createWarehouse();
        }
    }

    private void createWarehouse() {
        try {
            WarehouseCreateRequestDto dto = WarehouseCreateRequestDto.builder().name(name.getValue()).warehouseType(
                    warehouseType.getValue()).availableForSale(availableForSale.getValue()).status(status.getValue())
                    .build();

            warehouseService.createWarehouse(dto);
            NotificationUtils.success("Almacén creado exitosamente");
            clearForm();
            selectedWarehouse = null;
            isEditMode = false;
            deleteButton.setEnabled(false);
            refreshAll();
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage());
            NotificationUtils.error("Error al crear el almacén: " + ex.getMessage());
        }
    }

    private void updateWarehouse() {
        try {
            if (selectedWarehouse != null) {
                WarehouseUpdateRequestDto dto = WarehouseUpdateRequestDto.builder().name(name.getValue()).warehouseType(
                        warehouseType.getValue()).availableForSale(availableForSale.getValue()).status(status
                                .getValue()).build();

                warehouseService.updateWarehouse(selectedWarehouse.getId(), dto);
                NotificationUtils.success("Almacén actualizado exitosamente");
                clearForm();
                selectedWarehouse = null;
                isEditMode = false;
                deleteButton.setEnabled(false);
                refreshAll();
            }
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage());
            NotificationUtils.error("Error al actualizar el almacén: " + ex.getMessage());
        }
    }

    private void editWarehouse(Warehouse warehouse) {
        selectedWarehouse = warehouse;
        populateForm(warehouse);
        isEditMode = true;
    }

    private void deleteWarehouse(Warehouse warehouse) {
        try {
            warehouseService.deleteWarehouse(warehouse.getId());
            clearForm();
            selectedWarehouse = null;
            isEditMode = false;
            refreshAll();
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage());
            NotificationUtils.error("Error al eliminar el almacén: " + ex.getMessage());
        }
    }

    private void populateForm(Warehouse warehouse) {
        name.setValue(warehouse.getName());
        warehouseType.setValue(warehouse.getWarehouseType());
        availableForSale.setValue(warehouse.isAvailableForSale());
        status.setValue(warehouse.isStatus());

        updateCancelButtonState();
    }

    private void clearForm() {
        name.clear();
        warehouseType.clear();
        availableForSale.clear();
        status.clear();

        name.setInvalid(false);
        warehouseType.setInvalid(false);
        availableForSale.setInvalid(false);
        status.setInvalid(false);

        updateCancelButtonState();
    }

    private boolean validateForm() {
        boolean isValid = true;
        if (name.isEmpty()) {
            name.setInvalid(true);
            name.setErrorMessage("El nombre del almacén es obligatorio");
            isValid = false;
        } else {
            name.setInvalid(false);
        }
        if (warehouseType.isEmpty()) {
            warehouseType.setInvalid(true);
            warehouseType.setErrorMessage("El tipo de almacén es obligatorio");
            isValid = false;
        } else {
            warehouseType.setInvalid(false);
        }
        if (availableForSale.isEmpty()) {
            availableForSale.setInvalid(true);
            availableForSale.setErrorMessage("El campo 'Disponible para Venta' es obligatorio");
            isValid = false;
        } else {
            availableForSale.setInvalid(false);
        }
        if (status.isEmpty()) {
            status.setInvalid(true);
            status.setErrorMessage("El estado del almacén es obligatorio");
            isValid = false;
        } else {
            status.setInvalid(false);
        }
        return isValid;
    }

    private void refreshAll() {
        var warehouses = warehouseService.getAllWarehouses();
        warehouseDataProvider.getItems().clear();
        warehouseDataProvider.getItems().addAll(warehouses);
        warehouseDataProvider.refreshAll();
        applyFilters();
    }

    private void applyFilters() {
        String search = searchField.getValue() != null ? searchField.getValue().trim().toLowerCase() : "";
        String status = statusFilter.getValue();

        warehouseDataProvider.setFilter(warehouse -> {
            boolean matchesSearch = search.isEmpty() || warehouse.getName().toLowerCase().contains(search) || (warehouse
                    .getWarehouseType() != null && warehouse.getWarehouseType().name().toLowerCase().contains(search));
            boolean matchesStatus = "Todos".equals(status) || ("Activo".equals(status) && warehouse
                    .isStatus()) || ("Inactivo".equals(status) && !warehouse.isStatus());
            return matchesSearch && matchesStatus;
        });
    }

    private void updateQuantity() {
        try {
            long count = warehouseService.getAllWarehouses().stream().filter(Warehouse::isStatus).count();
            quantity.setText("Almacenes (" + count + ")");
        } catch (Exception e) {
            log.warn("Error getting warehouses" + " count", e);
            quantity.setText("Almacenes (0)");
        }
    }
}
