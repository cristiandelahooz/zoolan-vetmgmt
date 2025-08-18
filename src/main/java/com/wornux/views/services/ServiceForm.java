package com.wornux.views.services;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.Service;
import com.wornux.data.enums.ServiceCategory;
import com.wornux.data.enums.ServiceType;
import com.wornux.dto.request.ServiceCreateRequestDto;
import com.wornux.dto.request.ServiceUpdateRequestDto;
import com.wornux.services.interfaces.ServiceService;
import com.wornux.utils.NotificationUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class ServiceForm extends Dialog {

  // Form fields
  private final TextField name = new TextField("Nombre del Servicio");
  private final TextArea description = new TextArea("Descripción");
  private final ComboBox<ServiceCategory> serviceCategory = new ComboBox<>("Tipo de Servicio");
  private final NumberField price = new NumberField("Precio");

  // Form buttons
  private final Button saveButton = new Button("Guardar");
  private final Button cancelButton = new Button("Cancelar");

  // Header
  private final H3 headerTitle = new H3();

  // Binders
  private final Binder<ValidationBean> binder = new BeanValidationBinder<>(ValidationBean.class);
  private final Binder<ServiceUpdateRequestDto> binderUpdate = new BeanValidationBinder<>(ServiceUpdateRequestDto.class);

  // Services
  private final transient ServiceService serviceService;

  // State
  private boolean isEditMode = false;
  private Service currentService;
  private ValidationBean validationBean;
  private Runnable onSaveCallback;

  // Event listeners
  private final List<Consumer<ServiceCreateRequestDto>> serviceSavedListeners = new ArrayList<>();
  private final List<Runnable> serviceCancelledListeners = new ArrayList<>();

  public ServiceForm(ServiceService serviceService) {
    this.serviceService = serviceService;
    this.validationBean = new ValidationBean();

    setupDialog();
    setupForm();
    setupBinders();
    setupEventListeners();
  }

  private void setupDialog() {
    setModal(true);
    setDraggable(false);
    setResizable(false);
    setWidth("600px");
    setMaxWidth("90vw");
    addClassNames(LumoUtility.Padding.NONE);
  }

  private void setupForm() {
    setupFormComponents();
    FormLayout formLayout = createFormLayout();

    HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
    buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
    buttonLayout.setSpacing(true);
    buttonLayout.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.JustifyContent.END);

    VerticalLayout content = new VerticalLayout(headerTitle, formLayout, buttonLayout);
    content.setPadding(false);
    content.setSpacing(false);

    add(content);
  }

  private void setupFormComponents() {
    // Configure name field
    name.setPrefixComponent(VaadinIcon.TAG.create());
    name.setRequiredIndicatorVisible(true);
    name.setPlaceholder("Ej: Consulta General, Corte de Pelo");

    // Configure description field
    description.setPrefixComponent(VaadinIcon.TEXT_LABEL.create());
    description.setPlaceholder("Descripción detallada del servicio...");
    description.setMaxLength(500);
    description.setHelperText("Máximo 500 caracteres");

    // Configure service type
    serviceCategory.setItems(ServiceCategory.values());
    serviceCategory.setItemLabelGenerator(ServiceCategory::getDisplay);
    serviceCategory.setRequiredIndicatorVisible(true);
    serviceCategory.setPrefixComponent(VaadinIcon.CLIPBOARD_HEART.create());

    // Configure price field
    price.setPrefixComponent(VaadinIcon.DOLLAR.create());
    price.setRequiredIndicatorVisible(true);
    price.setMin(0);
    price.setStep(0.01);
    price.setPlaceholder("0.00");
    price.setHelperText("Precio en pesos dominicanos (DOP)");

    // Configure buttons
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
  }

  private FormLayout createFormLayout() {
    FormLayout formLayout = new FormLayout();
    formLayout.addClassNames(LumoUtility.Padding.MEDIUM);

    formLayout.add(
        name,
        serviceCategory,
        price,
        description
    );

    formLayout.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1),
        new FormLayout.ResponsiveStep("500px", 2)
    );

    // Make description span full width
    formLayout.setColspan(description, 2);

    return formLayout;
  }

  public void setOnSaveCallback(Runnable callback) {
    this.onSaveCallback = callback;
  }

  public void updateHeaderTitle(String title) {
    headerTitle.setText(title);
    headerTitle.addClassNames(LumoUtility.Margin.NONE, LumoUtility.Padding.MEDIUM);
  }

  private void setupBinders() {
    setupCreateBinder();
    setupUpdateBinder();
  }

  private void setupCreateBinder() {
    binder.forField(name)
        .asRequired("El nombre del servicio es requerido")
        .withValidator(new StringLengthValidator("El nombre debe tener al menos 2 caracteres", 2, 100))
        .bind(ValidationBean::getName, ValidationBean::setName);

    binder.forField(description)
        .withValidator(new StringLengthValidator("La descripción no puede exceder 500 caracteres", 0, 500))
        .bind(ValidationBean::getDescription, ValidationBean::setDescription);

    binder.forField(serviceCategory)
        .asRequired("El tipo de servicio es requerido")
        .bind(ValidationBean::getServiceCategory, ValidationBean::setServiceCategory);

    binder.forField(price)
        .withValidator(this::validatePrice)
        .bind(ValidationBean::getPrice, ValidationBean::setPrice);
  }

  private void setupUpdateBinder() {
    binderUpdate.forField(name)
        .asRequired("El nombre del servicio es requerido")
        .withValidator(new StringLengthValidator("El nombre debe tener al menos 2 caracteres", 2, 100))
        .bind(ServiceUpdateRequestDto::getName, ServiceUpdateRequestDto::setName);

    binderUpdate.forField(description)
        .withValidator(new StringLengthValidator("La descripción no puede exceder 500 caracteres", 0, 500))
        .bind(ServiceUpdateRequestDto::getDescription, ServiceUpdateRequestDto::setDescription);

    binderUpdate.forField(serviceCategory)
        .asRequired("El tipo de servicio es requerido")
        .bind(ServiceUpdateRequestDto::getServiceCategory, ServiceUpdateRequestDto::setServiceCategory);

    binderUpdate.forField(price)
        .withValidator(this::validatePrice)
        .bind(ServiceUpdateRequestDto::getPrice, ServiceUpdateRequestDto::setPrice);
  }

  private ValidationResult validatePrice(Double value, ValueContext context) {
    if (value == null) {
      return ValidationResult.error("El precio es requerido");
    }
    if (value < 0) {
      return ValidationResult.error("El precio no puede ser negativo");
    }
    if (value > 999999.99) {
      return ValidationResult.error("El precio no puede exceder 999,999.99");
    }
    return ValidationResult.ok();
  }

  private void setupEventListeners() {
    saveButton.addClickListener(this::save);
    cancelButton.addClickListener(e -> {
      close();
      fireServiceCancelledEvent();
    });
  }

  private void save(ClickEvent<Button> event) {
    if (isEditMode) {
      saveUpdate();
    } else {
      saveNew();
    }
  }

  private void saveNew() {
    if (!binder.writeBeanIfValid(validationBean)) {
      NotificationUtils.error("Por favor, corrija los errores en el formulario");
      return;
    }

    try {
      ServiceCreateRequestDto dto = ServiceCreateRequestDto.builder()
          .name(validationBean.getName().trim())
          .description(validationBean.getDescription() != null ? validationBean.getDescription().trim() : null)
          .serviceCategory(validationBean.getServiceCategory())
          .price(BigDecimal.valueOf(validationBean.getPrice()))
          .build();

      serviceService.save(dto);
      NotificationUtils.success("Servicio creado exitosamente");
      close();
      fireServiceSavedEvent(dto);
      if (onSaveCallback != null) {
        onSaveCallback.run();
      }
    } catch (Exception e) {
      log.error("Error saving service", e);
      NotificationUtils.error("Error al guardar el servicio: " + e.getMessage());
    }
  }

  private void saveUpdate() {
    if (!binderUpdate.isValid()) {
      NotificationUtils.error("Por favor, corrija los errores en el formulario");
      return;
    }

    try {
      ServiceUpdateRequestDto updateDto = binderUpdate.getBean();
      serviceService.updateService(currentService.getId(), updateDto);
      NotificationUtils.success("Servicio actualizado exitosamente");
      close();
      if (onSaveCallback != null) {
        onSaveCallback.run();
      }
    } catch (Exception e) {
      log.error("Error updating service", e);
      NotificationUtils.error("Error al actualizar el servicio: " + e.getMessage());
    }
  }

  public void openForNew() {
    isEditMode = false;
    currentService = null;
    validationBean = new ValidationBean();

    updateHeaderTitle("Nuevo Servicio");
    clearForm();

    binder.setBean(validationBean);

    name.focus();
    open();
  }

  public void openForEdit(Service service) {
    isEditMode = true;
    currentService = service;

    updateHeaderTitle("Editar Servicio: " + service.getName());

    populateForm(service);
    ServiceUpdateRequestDto updateDto = createUpdateDtoFromService(service);
    binderUpdate.setBean(updateDto);

    name.focus();
    open();
  }

  private void clearForm() {
    name.clear();
    description.clear();
    serviceCategory.clear();
    price.clear();
  }

  private void populateForm(Service service) {
    name.setValue(service.getName());
    description.setValue(service.getDescription() != null ? service.getDescription() : "");
    serviceCategory.setValue(service.getServiceCategory());
    price.setValue(service.getPrice().doubleValue());
  }

  private ServiceUpdateRequestDto createUpdateDtoFromService(Service service) {
    return new ServiceUpdateRequestDto(
        service.getName(),
        service.getDescription(),
        service.getServiceCategory(),
        service.getPrice().doubleValue()
    );
  }

  public void addServiceSavedListener(Consumer<ServiceCreateRequestDto> listener) {
    serviceSavedListeners.add(listener);
  }

  public void addServiceCancelledListener(Runnable listener) {
    serviceCancelledListeners.add(listener);
  }

  private void fireServiceSavedEvent(ServiceCreateRequestDto dto) {
    serviceSavedListeners.forEach(listener -> listener.accept(dto));
  }

  private void fireServiceCancelledEvent() {
    serviceCancelledListeners.forEach(Runnable::run);
  }

  @Getter
  @Setter
  public static class ValidationBean {
    private String name;
    private String description;
    private ServiceCategory serviceCategory;
    private Double price;
  }
}