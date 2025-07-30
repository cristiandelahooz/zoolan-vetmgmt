package com.wornux.views.warehouses;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.Warehouse;
import com.wornux.data.enums.WarehouseType;
import com.wornux.dto.request.WarehouseCreateRequestDto;
import com.wornux.dto.request.WarehouseUpdateRequestDto;
import com.wornux.services.interfaces.WarehouseService;
import com.wornux.utils.NotificationUtils;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WarehouseForm extends Dialog {

    private final TextField name = new TextField("Warehouse Name");
    private final ComboBox<WarehouseType> warehouseType = new ComboBox<>("Warehouse Type");
    private final ComboBox<Boolean> availableForSale = new ComboBox<>("Available for Sale");
    private final ComboBox<Boolean> status = new ComboBox<>("Status");

    private final Button saveButton = new Button("Save");
    private final Button cancelButton = new Button("Cancel");

    private final transient WarehouseService warehouseService;

    @Setter
    private transient Runnable onSaveCallback;

    private final List<Consumer<Warehouse>> warehouseSavedListeners = new ArrayList<>();
    private final List<Runnable> warehouseCancelledListeners = new ArrayList<>();

    private transient Warehouse currentWarehouse;
    private boolean isEditMode = false;

    public WarehouseForm(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;

        setHeaderTitle("New Warehouse");
        setModal(true);
        setWidth("500px");
        setHeight("400px");

        createForm();
        setupEventListeners();
    }

    private void createForm() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(name, warehouseType, availableForSale, status);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        name.setRequired(true);
        name.setRequiredIndicatorVisible(true);

        warehouseType.setItems(WarehouseType.values());
        warehouseType.setRequired(true);
        warehouseType.setRequiredIndicatorVisible(true);

        availableForSale.setItems(true, false);
        availableForSale.setItemLabelGenerator(val -> val ? "Yes" : "No");
        availableForSale.setRequired(true);
        availableForSale.setRequiredIndicatorVisible(true);

        status.setItems(true, false);
        status.setItemLabelGenerator(val -> val ? "Active" : "Inactive");
        status.setRequired(true);
        status.setRequiredIndicatorVisible(true);

        VerticalLayout content = new VerticalLayout();
        content.add(new H3("Warehouse Information"), formLayout);
        content.addClassNames(LumoUtility.Padding.MEDIUM);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
        buttonLayout.addClassNames(LumoUtility.JustifyContent.END, LumoUtility.Gap.MEDIUM);

        add(content, buttonLayout);
    }

    private void setupEventListeners() {
        saveButton.addClickListener(this::save);
        cancelButton.addClickListener(e -> {
            fireWarehouseCancelledEvent();
            close();
        });
    }

    private void save(ClickEvent<Button> event) {
        if (!validateForm()) {
            NotificationUtils.error("Please complete all required fields");
            return;
        }

        if (isEditMode && currentWarehouse != null) {
            updateWarehouse();
        } else {
            createWarehouse();
        }
    }

    private void createWarehouse() {
        WarehouseCreateRequestDto dto = WarehouseCreateRequestDto.builder()
                .name(name.getValue())
                .warehouseType(warehouseType.getValue())
                .availableForSale(availableForSale.getValue())
                .status(status.getValue())
                .build();

        warehouseService.createWarehouse(dto);
        NotificationUtils.success("Warehouse created successfully");

        fireWarehouseSavedEvent(null);

        if (onSaveCallback != null) {
            onSaveCallback.run();
        }

        close();
    }

    private void updateWarehouse() {
        WarehouseUpdateRequestDto dto = WarehouseUpdateRequestDto.builder()
                .name(name.getValue())
                .warehouseType(warehouseType.getValue())
                .availableForSale(availableForSale.getValue())
                .status(status.getValue())
                .build();

        Warehouse updatedWarehouse = warehouseService.updateWarehouse(currentWarehouse.getId(), dto);
        NotificationUtils.success("Warehouse updated successfully");

        fireWarehouseSavedEvent(updatedWarehouse);

        if (onSaveCallback != null) {
            onSaveCallback.run();
        }

        close();
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (name.isEmpty()) {
            name.setInvalid(true);
            isValid = false;
        } else {
            name.setInvalid(false);
        }

        if (warehouseType.isEmpty()) {
            warehouseType.setInvalid(true);
            isValid = false;
        } else {
            warehouseType.setInvalid(false);
        }

        if (availableForSale.isEmpty()) {
            availableForSale.setInvalid(true);
            isValid = false;
        } else {
            availableForSale.setInvalid(false);
        }

        return isValid;
    }

    public void openForNew() {
        setHeaderTitle("New Warehouse");
        isEditMode = false;
        currentWarehouse = null;
        clearForm();
        name.focus();
        open();
    }

    public void openForEdit(Warehouse warehouse) {
        setHeaderTitle("Edit Warehouse");
        isEditMode = true;
        currentWarehouse = warehouse;
        clearForm();
        populateForm(warehouse);
        name.focus();
        open();
    }

    private void populateForm(Warehouse warehouse) {
        name.setValue(warehouse.getName());
        warehouseType.setValue(warehouse.getWarehouseType());
        availableForSale.setValue(warehouse.isAvailableForSale());
    }

    private void clearForm() {
        name.clear();
        warehouseType.clear();
        availableForSale.clear();
    }

    public void addWarehouseSavedListener(Consumer<Warehouse> listener) {
        warehouseSavedListeners.add(listener);
    }

    public void addWarehouseCancelledListener(Runnable listener) {
        warehouseCancelledListeners.add(listener);
    }

    private void fireWarehouseSavedEvent(Warehouse warehouse) {
        warehouseSavedListeners.forEach(listener -> listener.accept(warehouse));
    }

    private void fireWarehouseCancelledEvent() {
        warehouseCancelledListeners.forEach(Runnable::run);
    }
}