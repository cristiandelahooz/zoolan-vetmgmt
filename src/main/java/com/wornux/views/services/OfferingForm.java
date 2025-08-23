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
import com.wornux.data.entity.Offering;
import com.wornux.data.enums.OfferingType;
import com.wornux.dto.request.ServiceCreateRequestDto;
import com.wornux.dto.request.ServiceUpdateRequestDto;
import com.wornux.services.interfaces.OfferingService;
import com.wornux.utils.NotificationUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OfferingForm extends Dialog {

  private final TextField name = new TextField("Nombre del Servicio");
  private final TextArea description = new TextArea("Descripción");
  private final ComboBox<OfferingType> serviceType = new ComboBox<>("Tipo de Servicio");
  private final NumberField price = new NumberField("Precio");

  private final Button saveButton = new Button("Guardar");
  private final Button cancelButton = new Button("Cancelar");

  private final H3 headerTitle = new H3();

  private final Binder<ValidationBean> binder = new BeanValidationBinder<>(ValidationBean.class);
  private final Binder<ServiceUpdateRequestDto> binderUpdate =
      new BeanValidationBinder<>(ServiceUpdateRequestDto.class);

  private final transient OfferingService serviceService;
  private final List<Consumer<ServiceCreateRequestDto>> serviceSavedListeners = new ArrayList<>();
  private final List<Runnable> serviceCancelledListeners = new ArrayList<>();
  private boolean isEditMode = false;
  private Offering currentOffering;
  private ValidationBean validationBean;
  private Runnable onSaveCallback;

  public OfferingForm(OfferingService serviceService) {
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
    name.setPrefixComponent(VaadinIcon.TAG.create());
    name.setRequiredIndicatorVisible(true);
    name.setPlaceholder("Ej: Consulta General, Corte de Pelo");

    description.setPrefixComponent(VaadinIcon.TEXT_LABEL.create());
    description.setPlaceholder("Descripción detallada del servicio...");
    description.setMaxLength(500);
    description.setHelperText("Máximo 500 caracteres");

    serviceType.setItems(OfferingType.values());
    serviceType.setItemLabelGenerator(OfferingType::getDisplay);
    serviceType.setRequiredIndicatorVisible(true);
    serviceType.setPrefixComponent(VaadinIcon.CLIPBOARD_HEART.create());

    price.setPrefixComponent(VaadinIcon.DOLLAR.create());
    price.setRequiredIndicatorVisible(true);
    price.setMin(0);
    price.setStep(0.01);
    price.setPlaceholder("0.00");
    price.setHelperText("Precio en pesos dominicanos (DOP)");

    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
  }

  private FormLayout createFormLayout() {
    FormLayout formLayout = new FormLayout();
    formLayout.addClassNames(LumoUtility.Padding.MEDIUM);

    formLayout.add(name, serviceType, price, description);

    formLayout.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

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
    binder
        .forField(name)
        .asRequired("El nombre del servicio es requerido")
        .withValidator(
            new StringLengthValidator("El nombre debe tener al menos 2 caracteres", 2, 100))
        .bind(ValidationBean::getName, ValidationBean::setName);

    binder
        .forField(description)
        .withValidator(
            new StringLengthValidator("La descripción no puede exceder 500 caracteres", 0, 500))
        .bind(ValidationBean::getDescription, ValidationBean::setDescription);

    binder
        .forField(serviceType)
        .asRequired("El tipo de servicio es requerido")
        .bind(ValidationBean::getServiceType, ValidationBean::setServiceType);

    binder
        .forField(price)
        .withValidator(this::validatePrice)
        .bind(ValidationBean::getPrice, ValidationBean::setPrice);
  }

  private void setupUpdateBinder() {
    binderUpdate
        .forField(name)
        .asRequired("El nombre del servicio es requerido")
        .withValidator(
            new StringLengthValidator("El nombre debe tener al menos 2 caracteres", 2, 100))
        .bind(ServiceUpdateRequestDto::getName, ServiceUpdateRequestDto::setName);

    binderUpdate
        .forField(description)
        .withValidator(
            new StringLengthValidator("La descripción no puede exceder 500 caracteres", 0, 500))
        .bind(ServiceUpdateRequestDto::getDescription, ServiceUpdateRequestDto::setDescription);

    binderUpdate
        .forField(serviceType)
        .asRequired("El tipo de servicio es requerido")
        .bind(ServiceUpdateRequestDto::getServiceType, ServiceUpdateRequestDto::setServiceType);

    binderUpdate
        .forField(price)
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
    cancelButton.addClickListener(
        e -> {
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
      ServiceCreateRequestDto dto =
          ServiceCreateRequestDto.builder()
              .name(validationBean.getName().trim())
              .description(
                  validationBean.getDescription() != null
                      ? validationBean.getDescription().trim()
                      : null)
              .serviceType(validationBean.getServiceType())
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
      log.error("Error saving offering", e);
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
      serviceService.updateService(currentOffering.getId(), updateDto);
      NotificationUtils.success("Servicio actualizado exitosamente");
      close();
      if (onSaveCallback != null) {
        onSaveCallback.run();
      }
    } catch (Exception e) {
      log.error("Error updating offering", e);
      NotificationUtils.error("Error al actualizar el servicio: " + e.getMessage());
    }
  }

  public void openForNew() {
    isEditMode = false;
    currentOffering = null;
    validationBean = new ValidationBean();

    updateHeaderTitle("Nuevo Servicio");
    clearForm();

    binder.setBean(validationBean);

    name.focus();
    open();
  }

  public void openForEdit(Offering offering) {
    isEditMode = true;
    currentOffering = offering;

    updateHeaderTitle("Editar Servicio: " + offering.getName());

    populateForm(offering);
    ServiceUpdateRequestDto updateDto = createUpdateDtoFromService(offering);
    binderUpdate.setBean(updateDto);

    name.focus();
    open();
  }

  private void clearForm() {
    name.clear();
    description.clear();
    serviceType.clear();
    price.clear();
  }

  private void populateForm(Offering offering) {
    name.setValue(offering.getName());
    description.setValue(offering.getDescription() != null ? offering.getDescription() : "");
    serviceType.setValue(offering.getServiceType());
    price.setValue(offering.getPrice().doubleValue());
  }

  private ServiceUpdateRequestDto createUpdateDtoFromService(Offering offering) {
    return new ServiceUpdateRequestDto(
        offering.getName(),
        offering.getDescription(),
        offering.getServiceType(),
        offering.getPrice().doubleValue());
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
    private OfferingType serviceType;
    private Double price;
  }
}
