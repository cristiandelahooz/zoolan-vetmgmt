package com.wornux.views.products;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.Product;
import com.wornux.data.entity.Supplier;
import com.wornux.data.entity.Warehouse;
import com.wornux.data.enums.ProductCategory;
import com.wornux.dto.request.ProductCreateRequestDto;
import com.wornux.dto.request.ProductUpdateRequestDto;
import com.wornux.services.interfaces.ProductService;
import com.wornux.services.interfaces.SupplierService;
import com.wornux.services.interfaces.WarehouseService;
import com.wornux.utils.NotificationUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductForm extends Dialog {

  // Product Information
  private final TextField name = new TextField("Nombre del Producto");
  private final TextArea description = new TextArea("Descripción");
  private final NumberField purchasePrice = new NumberField("Precio de Compra");
  private final NumberField salesPrice = new NumberField("Precio de Venta");
  private final NumberField accountingStock = new NumberField("Stock Contable");
  private final NumberField availableStock = new NumberField("Stock Disponible");
  private final NumberField reorderLevel = new NumberField("Nivel de Reorden");
  private final ComboBox<ProductCategory> category = new ComboBox<>("Categoría");
  private final ComboBox<Supplier> supplier = new ComboBox<>("Proveedor");
  private final ComboBox<Warehouse> warehouse = new ComboBox<>("Almacén");

  private final Button saveButton = new Button("Guardar");
  private final Button cancelButton = new Button("Cancelar");

  private final transient ProductService productService;
  private final transient SupplierService supplierService;
  private final transient WarehouseService warehouseService;

  @Setter private transient Runnable onSaveCallback;

  private final List<Consumer<Product>> productSavedListeners = new ArrayList<>();
  private final List<Runnable> productCancelledListeners = new ArrayList<>();

  private transient Product currentProduct;
  private boolean isEditMode = false;

  private static final String RESPONSIVE_STEP_WIDTH = "500px";

  public ProductForm(
      ProductService productService,
      SupplierService supplierService,
      WarehouseService warehouseService) {
    this.productService = productService;
    this.supplierService = supplierService;
    this.warehouseService = warehouseService;

    setHeaderTitle("Nuevo Producto");
    setModal(true);
    setWidth("700px");
    setHeight("600px");

    createForm();
    setupValidation();
    setupEventListeners();
  }

  private void createForm() {
    FormLayout productInfo = new FormLayout();
    productInfo.add(
        name,
        description,
        purchasePrice,
        salesPrice,
        accountingStock,
        availableStock,
        reorderLevel,
        category,
        supplier,
        warehouse);
    productInfo.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1),
        new FormLayout.ResponsiveStep(RESPONSIVE_STEP_WIDTH, 2));

    setupFields(productInfo);

    VerticalLayout content = new VerticalLayout();
    content.add(new H3("Información del Producto"), productInfo);

    content.addClassNames(LumoUtility.Padding.MEDIUM);

    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

    HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
    buttonLayout.addClassNames(LumoUtility.JustifyContent.END, LumoUtility.Gap.MEDIUM);

    add(content, buttonLayout);
  }

  private void setupFields(FormLayout productInfo) {
    name.setPlaceholder("Ej: Alimento Premium para Perros");
    name.setRequired(true);
    name.setRequiredIndicatorVisible(true);
    name.setErrorMessage("El nombre del producto es requerido");

    description.setPlaceholder("Descripción detallada del producto...");
    description.setHeight("100px");

    purchasePrice.setMin(0);
    purchasePrice.setValue(0.0);
    purchasePrice.setStepButtonsVisible(true);
    purchasePrice.setStep(0.01);
    purchasePrice.setRequired(true);
    purchasePrice.setRequiredIndicatorVisible(true);
    purchasePrice.setErrorMessage("El precio de compra es requerido");

    salesPrice.setMin(0);
    salesPrice.setValue(0.0);
    salesPrice.setStepButtonsVisible(true);
    salesPrice.setStep(0.01);
    salesPrice.setRequired(true);
    salesPrice.setRequiredIndicatorVisible(true);
    salesPrice.setErrorMessage("El precio de venta es requerido");

    accountingStock.setMin(0);
    accountingStock.setValue(0.0);
    accountingStock.setStepButtonsVisible(true);
    accountingStock.setStep(1);
    accountingStock.setRequired(true);
    accountingStock.setRequiredIndicatorVisible(true);
    accountingStock.setErrorMessage("El stock contable es requerido");

    availableStock.setMin(0);
    availableStock.setValue(0.0);
    availableStock.setStepButtonsVisible(true);
    availableStock.setStep(1);
    availableStock.setRequired(true);
    availableStock.setRequiredIndicatorVisible(true);
    availableStock.setErrorMessage("El stock disponible es requerido");

    reorderLevel.setMin(0);
    reorderLevel.setValue(5.0);
    reorderLevel.setStepButtonsVisible(true);
    reorderLevel.setStep(1);
    reorderLevel.setHelperText("Cantidad mínima antes de reabastecer");

    category.setItems(ProductCategory.values());
    category.setItemLabelGenerator(this::getCategoryDisplayName);
    category.setRequired(true);
    category.setRequiredIndicatorVisible(true);
    category.setErrorMessage("La categoría es requerida");

    supplier.setItems(supplierService.getAllSuppliers());
    supplier.setItemLabelGenerator(Supplier::getCompanyName);
    supplier.setRequired(true);
    supplier.setRequiredIndicatorVisible(true);
    supplier.setErrorMessage("El proveedor es requerido");

    warehouse.setItems(
        warehouseService.getAllWarehouses().stream()
            .map(
                dto -> {
                  Warehouse w = new Warehouse();
                  w.setId(dto.getId());
                  w.setName(dto.getName());
                  return w;
                })
            .toList());
    warehouse.setItemLabelGenerator(Warehouse::getName);
    warehouse.setRequired(true);
    warehouse.setRequiredIndicatorVisible(true);
    warehouse.setErrorMessage("El almacén es requerido");

    productInfo.setColspan(description, 2);
  }

  private String getCategoryDisplayName(ProductCategory category) {
    return switch (category) {
      case ALIMENTO -> "Alimento";
      case MEDICINA -> "Medicina";
      case ACCESORIO -> "Accesorio";
      case HIGIENE -> "Higiene";
      case OTRO -> "Otro";
    };
  }

  private void setupValidation() {
    // Basic field requirements are already set in setupFields()
  }

  private void setupEventListeners() {
    saveButton.addClickListener(this::save);
    cancelButton.addClickListener(
        e -> {
          fireProductCancelledEvent();
          close();
        });
  }

  public void save(ClickEvent<Button> event) {
    try {
      if (!validateForm()) {
        NotificationUtils.error("Por favor, complete todos los campos requeridos");
        return;
      }

      if (isEditMode && currentProduct != null) {
        updateProduct();
      } else {
        createProduct();
      }

    } catch (Exception e) {
      log.error("Error saving product", e);
      NotificationUtils.error("Error al guardar producto: " + e.getMessage());
    }
  }

  public void delete() {
    if (currentProduct == null) {
      NotificationUtils.error("No hay producto seleccionado para eliminar");
      return;
    }

    productService.delete(currentProduct.getId());
    NotificationUtils.success("Producto eliminado exitosamente");

    fireProductCancelledEvent();

    if (onSaveCallback != null) {
      onSaveCallback.run();
    }

    close();
  }

  private void createProduct() {
    ProductCreateRequestDto dto =
        ProductCreateRequestDto.builder()
            .name(name.getValue())
            .description(description.getValue())
            .purchasePrice(BigDecimal.valueOf(purchasePrice.getValue()))
            .salesPrice(BigDecimal.valueOf(salesPrice.getValue()))
            .accountingStock(accountingStock.getValue().intValue())
            .availableStock(availableStock.getValue().intValue())
            .reorderLevel(reorderLevel.getValue().intValue())
            .supplierId(supplier.getValue().getId())
            .category(category.getValue())
            .warehouseId(warehouse.getValue().getId())
            .build();

    productService.createProduct(dto);
    NotificationUtils.success("Producto creado exitosamente");

    fireProductSavedEvent(null);

    if (onSaveCallback != null) {
      onSaveCallback.run();
    }

    close();
  }

  private void updateProduct() {
    ProductUpdateRequestDto dto =
        ProductUpdateRequestDto.builder()
            .name(name.getValue())
            .description(description.getValue())
            .purchasePrice(BigDecimal.valueOf(purchasePrice.getValue()))
            .salesPrice(BigDecimal.valueOf(salesPrice.getValue()))
            .accountingStock(accountingStock.getValue().intValue())
            .availableStock(availableStock.getValue().intValue())
            .reorderLevel(reorderLevel.getValue().intValue())
            .supplierId(supplier.getValue().getId())
            .category(category.getValue())
            .warehouseId(warehouse.getValue().getId())
            .build();

    Product updatedProduct = productService.update(currentProduct.getId(), dto);
    NotificationUtils.success("Producto actualizado exitosamente");

    fireProductSavedEvent(updatedProduct);

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

    if (purchasePrice.isEmpty() || purchasePrice.getValue() < 0) {
      purchasePrice.setInvalid(true);
      isValid = false;
    } else {
      purchasePrice.setInvalid(false);
    }

    if (salesPrice.isEmpty() || salesPrice.getValue() < 0) {
      salesPrice.setInvalid(true);
      isValid = false;
    } else {
      salesPrice.setInvalid(false);
    }

    if (accountingStock.isEmpty() || accountingStock.getValue() < 0) {
      accountingStock.setInvalid(true);
      isValid = false;
    } else {
      accountingStock.setInvalid(false);
    }

    if (availableStock.isEmpty() || availableStock.getValue() < 0) {
      availableStock.setInvalid(true);
      isValid = false;
    } else {
      availableStock.setInvalid(false);
    }

    if (category.isEmpty()) {
      category.setInvalid(true);
      isValid = false;
    } else {
      category.setInvalid(false);
    }

    if (supplier.isEmpty()) {
      supplier.setInvalid(true);
      isValid = false;
    } else {
      supplier.setInvalid(false);
    }

    if (warehouse.isEmpty()) {
      warehouse.setInvalid(true);
      isValid = false;
    } else {
      warehouse.setInvalid(false);
    }

    return isValid;
  }

  public void openForNew() {
    setHeaderTitle("Nuevo Producto");
    isEditMode = false;
    currentProduct = null;
    clearForm();
    enableAllFields();
    resetValidationStates();
    name.focus();
    open();
  }

  public void openForEdit(Product product) {
    setHeaderTitle("Editar Producto");
    isEditMode = true;
    currentProduct = product;
    clearForm();
    populateForm(product);
    enableAllFields();
    resetValidationStates();
    name.focus();
    open();
  }

  private void populateForm(Product product) {
    name.setValue(product.getName());
    description.setValue(product.getDescription() != null ? product.getDescription() : "");
    purchasePrice.setValue(
        product.getPurchasePrice() != null ? product.getPurchasePrice().doubleValue() : 0.0);
    salesPrice.setValue(
        product.getSalesPrice() != null ? product.getSalesPrice().doubleValue() : 0.0);
    accountingStock.setValue((double) product.getAccountingStock());
    availableStock.setValue((double) product.getAvailableStock());
    reorderLevel.setValue((double) product.getReorderLevel());
    category.setValue(product.getCategory());
    supplier.setValue(product.getSupplier());
    warehouse.setValue(product.getWarehouse());
  }

  private void clearForm() {
    name.clear();
    description.clear();
    purchasePrice.setValue(0.0);
    salesPrice.setValue(0.0);
    accountingStock.setValue(0.0);
    availableStock.setValue(0.0);
    reorderLevel.setValue(5.0);
    category.clear();
    supplier.clear();
    warehouse.clear();
  }

  private void enableAllFields() {
    name.setEnabled(true);
    description.setEnabled(true);
    purchasePrice.setEnabled(true);
    salesPrice.setEnabled(true);
    accountingStock.setEnabled(true);
    availableStock.setEnabled(true);
    reorderLevel.setEnabled(true);
    category.setEnabled(true);
    supplier.setEnabled(true);
    warehouse.setEnabled(true);
    saveButton.setEnabled(true);
  }

  private void resetValidationStates() {
    name.setInvalid(false);
    purchasePrice.setInvalid(false);
    salesPrice.setInvalid(false);
    accountingStock.setInvalid(false);
    availableStock.setInvalid(false);
    category.setInvalid(false);
    supplier.setInvalid(false);
    warehouse.setInvalid(false);
  }

  public void addProductSavedListener(Consumer<Product> listener) {
    productSavedListeners.add(listener);
  }

  public void addProductCancelledListener(Runnable listener) {
    productCancelledListeners.add(listener);
  }

  private void fireProductSavedEvent(Product product) {
    productSavedListeners.forEach(listener -> listener.accept(product));
  }

  private void fireProductCancelledEvent() {
    productCancelledListeners.forEach(Runnable::run);
  }
}
