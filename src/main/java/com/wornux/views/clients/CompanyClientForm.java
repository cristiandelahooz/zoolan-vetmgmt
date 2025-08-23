package com.wornux.views.clients;

import static com.wornux.constants.ValidationConstants.*;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.Client;
import com.wornux.data.enums.*;
import com.wornux.dto.request.ClientCreateRequestDto;
import com.wornux.dto.request.ClientUpdateRequestDto;
import com.wornux.services.interfaces.ClientService;
import com.wornux.utils.NotificationUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompanyClientForm extends Dialog {

  private final TextField companyName = new TextField("Nombre de la Empresa");
  private final TextField rnc = new TextField("RNC");
  private final EmailField email = new EmailField("Correo Electrónico");
  private final TextField phoneNumber = new TextField("Teléfono");

  private final ComboBox<PreferredContactMethod> preferredContactMethod =
      new ComboBox<>("Método de Contacto Preferido");
  private final TextField emergencyContactName = new TextField("Nombre del Contacto de Emergencia");
  private final TextField emergencyContactNumber = new TextField("Teléfono de Emergencia");

  private final TextField province = new TextField("Provincia");
  private final TextField municipality = new TextField("Municipio");
  private final TextField sector = new TextField("Sector");
  private final TextField streetAddress = new TextField("Dirección");
  private final TextArea referencePoints = new TextArea("Puntos de Referencia");

  private final NumberField creditLimit = new NumberField("Límite de Crédito");
  private final NumberField paymentTermsDays = new NumberField("Días de Términos de Pago");
  private final ComboBox<ClientRating> rating = new ComboBox<>("Calificación");
  private final ComboBox<ReferenceSource> referenceSource = new ComboBox<>("Fuente de Referencia");
  private final TextArea notes = new TextArea("Notas");

  private final Button saveButton = new Button("Guardar");
  private final Button cancelButton = new Button("Cancelar");

  private final Binder<ValidationBean> binder = new BeanValidationBinder<>(ValidationBean.class);
  private final Binder<ClientUpdateRequestDto> binderUpdate =
      new BeanValidationBinder<>(ClientUpdateRequestDto.class);
  private ValidationBean validationBean = new ValidationBean();
  private final transient ClientService clientService;
  private final List<Consumer<ClientCreateRequestDto>> clientSavedListeners = new ArrayList<>();
  private final List<Runnable> clientCancelledListeners = new ArrayList<>();
  @Setter private Runnable onSaveCallback;

  private boolean isEditMode = false;
  private Client currentClient = null;

  public CompanyClientForm(ClientService clientService) {
    this.clientService = clientService;

    setHeaderTitle("Nueva Empresa");
    setModal(true);
    setWidth("900px");
    setMaxWidth("95vw");
    setHeight("85vh");
    setMaxHeight("95vh");

    getHeader()
        .getElement()
        .getStyle()
        .set("background", "var(--lumo-primary-color-10pct)")
        .set("color", "var(--lumo-primary-text-color)");

    createForm();
    setupValidation();
    setupEventListeners();
  }

  private void createForm() {
    FormLayout companyInfo = new FormLayout();
    companyInfo.add(companyName, rnc, email, phoneNumber);
    companyInfo.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

    FormLayout contactInfo = new FormLayout();
    contactInfo.add(preferredContactMethod, emergencyContactName, emergencyContactNumber);
    contactInfo.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

    FormLayout addressInfo = new FormLayout();
    addressInfo.add(province, municipality, sector, streetAddress);
    addressInfo.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

    FormLayout businessInfo = new FormLayout();
    businessInfo.add(creditLimit, paymentTermsDays, rating, referenceSource);
    businessInfo.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

    preferredContactMethod.setItems(PreferredContactMethod.values());
    preferredContactMethod.setItemLabelGenerator(PreferredContactMethod::name);

    rating.setItems(ClientRating.values());
    rating.setItemLabelGenerator(ClientRating::name);
    rating.setValue(ClientRating.BUENO);

    referenceSource.setItems(ReferenceSource.values());
    referenceSource.setItemLabelGenerator(ReferenceSource::name);

    creditLimit.setMin(0);
    creditLimit.setValue(0.0);
    paymentTermsDays.setMin(0);
    paymentTermsDays.setValue(0.0);
    paymentTermsDays.setStep(1);

    referencePoints.setMaxLength(500);
    notes.setMaxLength(1000);

    rnc.setPlaceholder("Ej: 123456789");
    rnc.setHelperText("9 dígitos (empresa) u 11 dígitos (persona física)");

    configureFieldsForRealTimeValidation();

    addIconsToFields();

    VerticalLayout content = new VerticalLayout();
    content.add(
        new H3("Información de la Empresa"),
        companyInfo,
        new H3("Información de Contacto"),
        contactInfo,
        new H3("Dirección"),
        addressInfo,
        referencePoints,
        new H3("Información Comercial"),
        businessInfo,
        notes);

    content.addClassNames(LumoUtility.Padding.MEDIUM);

    HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
    buttonLayout.addClassNames(LumoUtility.JustifyContent.END, LumoUtility.Gap.MEDIUM);

    add(content, buttonLayout);
  }

  private void configureFieldsForRealTimeValidation() {
    companyName.setValueChangeMode(ValueChangeMode.EAGER);
    rnc.setValueChangeMode(ValueChangeMode.EAGER);
    email.setValueChangeMode(ValueChangeMode.EAGER);
    phoneNumber.setValueChangeMode(ValueChangeMode.EAGER);
    emergencyContactName.setValueChangeMode(ValueChangeMode.EAGER);
    emergencyContactNumber.setValueChangeMode(ValueChangeMode.EAGER);
    province.setValueChangeMode(ValueChangeMode.EAGER);
    municipality.setValueChangeMode(ValueChangeMode.EAGER);
    sector.setValueChangeMode(ValueChangeMode.EAGER);
    streetAddress.setValueChangeMode(ValueChangeMode.EAGER);
    referencePoints.setValueChangeMode(ValueChangeMode.EAGER);
    notes.setValueChangeMode(ValueChangeMode.EAGER);

    companyName.setRequiredIndicatorVisible(true);
    rnc.setRequiredIndicatorVisible(true);
    email.setRequiredIndicatorVisible(true);
    phoneNumber.setRequiredIndicatorVisible(true);
    province.setRequiredIndicatorVisible(true);
    municipality.setRequiredIndicatorVisible(true);
    sector.setRequiredIndicatorVisible(true);
    streetAddress.setRequiredIndicatorVisible(true);

    email.setErrorMessage("Proporcione un correo electrónico válido");
    province.setErrorMessage("La provincia es requerida");
    municipality.setErrorMessage("El municipio es requerido");
    sector.setErrorMessage("El sector es requerido");
    streetAddress.setErrorMessage("La dirección es requerida");
  }

  private void addIconsToFields() {
    companyName.setPrefixComponent(VaadinIcon.BUILDING.create());
    rnc.setPrefixComponent(VaadinIcon.CREDIT_CARD.create());
    email.setPrefixComponent(VaadinIcon.ENVELOPE.create());
    phoneNumber.setPrefixComponent(VaadinIcon.PHONE.create());
    preferredContactMethod.setPrefixComponent(VaadinIcon.CONNECT.create());
    emergencyContactName.setPrefixComponent(VaadinIcon.USERS.create());
    emergencyContactNumber.setPrefixComponent(VaadinIcon.PHONE_LANDLINE.create());
    province.setPrefixComponent(VaadinIcon.LOCATION_ARROW.create());
    municipality.setPrefixComponent(VaadinIcon.HOME.create());
    sector.setPrefixComponent(VaadinIcon.MAP_MARKER.create());
    streetAddress.setPrefixComponent(VaadinIcon.ROAD.create());
    referencePoints.setPrefixComponent(VaadinIcon.INFO_CIRCLE.create());
    creditLimit.setPrefixComponent(VaadinIcon.DOLLAR.create());
    paymentTermsDays.setPrefixComponent(VaadinIcon.CALENDAR_CLOCK.create());
    rating.setPrefixComponent(VaadinIcon.STAR.create());
    referenceSource.setPrefixComponent(VaadinIcon.QUESTION_CIRCLE.create());
    notes.setPrefixComponent(VaadinIcon.EDIT.create());
  }

  private void setupValidation() {
    setupCreateBinder();
    setupUpdateBinder();
  }

  private void setupCreateBinder() {
    binder
        .forField(companyName)
        .asRequired("El nombre de la empresa es requerido")
        .withValidator(
            new StringLengthValidator("El nombre debe tener al menos 2 caracteres", 2, null))
        .bind(ValidationBean::getCompanyName, ValidationBean::setCompanyName);

    binder
        .forField(rnc)
        .asRequired("El RNC es requerido")
        .withValidator(new RegexpValidator("El RNC debe contener 9 u 11 dígitos", RNC_PATTERN))
        .bind(ValidationBean::getRnc, ValidationBean::setRnc);

    binder
        .forField(email)
        .asRequired("El correo electrónico es requerido")
        .withValidator(new EmailValidator("Proporcione un correo electrónico válido"))
        .bind(ValidationBean::getEmail, ValidationBean::setEmail);

    binder
        .forField(phoneNumber)
        .asRequired("El teléfono es requerido")
        .withValidator(
            new RegexpValidator(
                "Proporcione un número de teléfono válido (809, 849 o 829 seguido de 7 dígitos)",
                DOMINICAN_PHONE_PATTERN))
        .bind(ValidationBean::getPhoneNumber, ValidationBean::setPhoneNumber);

    binder
        .forField(emergencyContactNumber)
        .withValidator(this::validateEmergencyPhone)
        .bind(ValidationBean::getEmergencyContactNumber, ValidationBean::setEmergencyContactNumber);

    binder
        .forField(province)
        .asRequired("La provincia es requerida")
        .withValidator(new StringLengthValidator("La provincia no puede estar vacía", 1, null))
        .bind(ValidationBean::getProvince, ValidationBean::setProvince);

    binder
        .forField(municipality)
        .asRequired("El municipio es requerido")
        .withValidator(new StringLengthValidator("El municipio no puede estar vacío", 1, null))
        .bind(ValidationBean::getMunicipality, ValidationBean::setMunicipality);

    binder
        .forField(sector)
        .asRequired("El sector es requerido")
        .withValidator(new StringLengthValidator("El sector no puede estar vacío", 1, null))
        .bind(ValidationBean::getSector, ValidationBean::setSector);

    binder
        .forField(streetAddress)
        .asRequired("La dirección es requerida")
        .withValidator(new StringLengthValidator("La dirección no puede estar vacía", 1, null))
        .bind(ValidationBean::getStreetAddress, ValidationBean::setStreetAddress);

    binder
        .forField(preferredContactMethod)
        .bind(ValidationBean::getPreferredContactMethod, ValidationBean::setPreferredContactMethod);

    binder
        .forField(emergencyContactName)
        .bind(ValidationBean::getEmergencyContactName, ValidationBean::setEmergencyContactName);

    binder.forField(rating).bind(ValidationBean::getRating, ValidationBean::setRating);

    binder
        .forField(referenceSource)
        .bind(ValidationBean::getReferenceSource, ValidationBean::setReferenceSource);

    binder
        .forField(referencePoints)
        .withValidator(
            new StringLengthValidator(
                "Los puntos de referencia no pueden exceder 500 caracteres", 0, 500))
        .bind(ValidationBean::getReferencePoints, ValidationBean::setReferencePoints);

    binder
        .forField(creditLimit)
        .asRequired("El límite de crédito es requerido")
        .withValidator(this::validateCreditLimit)
        .bind(ValidationBean::getCreditLimit, ValidationBean::setCreditLimit);

    binder
        .forField(paymentTermsDays)
        .asRequired("Los días de término de pago son requeridos")
        .withValidator(this::validatePaymentTermsDays)
        .withConverter(
            value -> value != null ? value.intValue() : null,
            value -> value != null ? value.doubleValue() : null)
        .bind(ValidationBean::getPaymentTermsDays, ValidationBean::setPaymentTermsDays);

    binder
        .forField(notes)
        .withValidator(
            new StringLengthValidator("Las notas no pueden exceder 1000 caracteres", 0, 1000))
        .bind(ValidationBean::getNotes, ValidationBean::setNotes);
  }

  private ValidationResult validateEmergencyPhone(String value, ValueContext context) {
    if (value == null || value.trim().isEmpty()) {
      return ValidationResult.ok();
    }
    if (value.matches(DOMINICAN_PHONE_PATTERN)) {
      return ValidationResult.ok();
    }
    return ValidationResult.error(
        "Proporcione un número de emergencia válido (809, 849 o 829 seguido de 7 dígitos)");
  }

  private ValidationResult validateCreditLimit(Double value, ValueContext context) {
    if (value == null) {
      return ValidationResult.ok();
    }
    if (value >= 0) {
      return ValidationResult.ok();
    }
    return ValidationResult.error("El límite de crédito no puede ser negativo");
  }

  private ValidationResult validatePaymentTermsDays(Double value, ValueContext context) {
    if (value == null) {
      return ValidationResult.ok();
    }
    if (value >= 0 && value % 1 == 0) {
      return ValidationResult.ok();
    }
    return ValidationResult.error(
        "Los días de término de pago deben ser un número entero no negativo");
  }

  private void setupUpdateBinder() {
    binderUpdate
        .forField(companyName)
        .asRequired("El nombre de la empresa es requerido")
        .withValidator(
            new StringLengthValidator("El nombre debe tener al menos 2 caracteres", 2, null))
        .bind(ClientUpdateRequestDto::getCompanyName, ClientUpdateRequestDto::setCompanyName);

    binderUpdate
        .forField(rnc)
        .asRequired("El RNC es requerido")
        .withValidator(new RegexpValidator("El RNC debe contener 9 u 11 dígitos", RNC_PATTERN))
        .bind(ClientUpdateRequestDto::getRnc, ClientUpdateRequestDto::setRnc);

    binderUpdate
        .forField(email)
        .asRequired("El correo electrónico es requerido")
        .withValidator(new EmailValidator("Proporcione un correo electrónico válido"))
        .bind(ClientUpdateRequestDto::getEmail, ClientUpdateRequestDto::setEmail);

    binderUpdate
        .forField(phoneNumber)
        .asRequired("El teléfono es requerido")
        .withValidator(
            new RegexpValidator(
                "Proporcione un número de teléfono válido (809, 849 o 829 seguido de 7 dígitos)",
                DOMINICAN_PHONE_PATTERN))
        .bind(ClientUpdateRequestDto::getPhoneNumber, ClientUpdateRequestDto::setPhoneNumber);

    binderUpdate
        .forField(preferredContactMethod)
        .bind(
            ClientUpdateRequestDto::getPreferredContactMethod,
            ClientUpdateRequestDto::setPreferredContactMethod);

    binderUpdate
        .forField(emergencyContactName)
        .bind(
            ClientUpdateRequestDto::getEmergencyContactName,
            ClientUpdateRequestDto::setEmergencyContactName);

    binderUpdate
        .forField(emergencyContactNumber)
        .withValidator(this::validateEmergencyPhone)
        .bind(
            ClientUpdateRequestDto::getEmergencyContactNumber,
            ClientUpdateRequestDto::setEmergencyContactNumber);

    binderUpdate
        .forField(rating)
        .bind(ClientUpdateRequestDto::getRating, ClientUpdateRequestDto::setRating);

    binderUpdate
        .forField(referenceSource)
        .bind(
            ClientUpdateRequestDto::getReferenceSource, ClientUpdateRequestDto::setReferenceSource);

    binderUpdate
        .forField(province)
        .asRequired("La provincia es requerida")
        .withValidator(new StringLengthValidator("La provincia no puede estar vacía", 1, null))
        .bind(ClientUpdateRequestDto::getProvince, ClientUpdateRequestDto::setProvince);

    binderUpdate
        .forField(municipality)
        .asRequired("El municipio es requerido")
        .withValidator(new StringLengthValidator("El municipio no puede estar vacío", 1, null))
        .bind(ClientUpdateRequestDto::getMunicipality, ClientUpdateRequestDto::setMunicipality);

    binderUpdate
        .forField(sector)
        .asRequired("El sector es requerido")
        .withValidator(new StringLengthValidator("El sector no puede estar vacío", 1, null))
        .bind(ClientUpdateRequestDto::getSector, ClientUpdateRequestDto::setSector);

    binderUpdate
        .forField(streetAddress)
        .asRequired("La dirección es requerida")
        .withValidator(new StringLengthValidator("La dirección no puede estar vacía", 1, null))
        .bind(ClientUpdateRequestDto::getStreetAddress, ClientUpdateRequestDto::setStreetAddress);

    binderUpdate
        .forField(referencePoints)
        .withValidator(
            new StringLengthValidator(
                "Los puntos de referencia no pueden exceder 500 caracteres", 0, 500))
        .bind(
            ClientUpdateRequestDto::getReferencePoints, ClientUpdateRequestDto::setReferencePoints);

    binderUpdate
        .forField(creditLimit)
        .withValidator(this::validateCreditLimit)
        .bind(ClientUpdateRequestDto::getCreditLimit, ClientUpdateRequestDto::setCreditLimit);

    binderUpdate
        .forField(paymentTermsDays)
        .withValidator(this::validatePaymentTermsDays)
        .withConverter(
            value -> value != null ? value.intValue() : null,
            value -> value != null ? value.doubleValue() : null)
        .bind(
            ClientUpdateRequestDto::getPaymentTermsDays,
            ClientUpdateRequestDto::setPaymentTermsDays);

    binderUpdate
        .forField(notes)
        .withValidator(
            new StringLengthValidator("Las notas no pueden exceder 1000 caracteres", 0, 1000))
        .bind(ClientUpdateRequestDto::getNotes, ClientUpdateRequestDto::setNotes);
  }

  private void setupEventListeners() {
    saveButton.addClickListener(this::save);
    cancelButton.addClickListener(
        e -> {
          fireClientCancelledEvent();
          close();
        });

    email.addValueChangeListener(e -> binder.validate());
    province.addValueChangeListener(e -> binder.validate());
    municipality.addValueChangeListener(e -> binder.validate());
    sector.addValueChangeListener(e -> binder.validate());
    streetAddress.addValueChangeListener(e -> binder.validate());
  }

  private void save(ClickEvent<Button> event) {
    try {
      if (isEditMode) {
        saveUpdate();
      } else {
        saveNew();
      }
    } catch (Exception e) {
      log.error("Error saving company client", e);
      NotificationUtils.error("Error al guardar empresa: " + e.getMessage());
    }
  }

  private void saveNew() {
    if (!binder.isValid()) {
      NotificationUtils.error("Por favor, corrija los errores en el formulario");
      binder.validate();
      return;
    }

    ClientCreateRequestDto dto =
        new ClientCreateRequestDto(
            email.getValue(),
            null,
            null,
            phoneNumber.getValue(),
            null,
            null,
            null,
            null,
            rnc.getValue(),
            companyName.getValue(),
            preferredContactMethod.getValue(),
            emergencyContactName.getValue(),
            emergencyContactNumber.getValue(),
            rating.getValue(),
            creditLimit.getValue(),
            paymentTermsDays.getValue() != null ? paymentTermsDays.getValue().intValue() : null,
            notes.getValue(),
            referenceSource.getValue(),
            province.getValue(),
            municipality.getValue(),
            sector.getValue(),
            streetAddress.getValue(),
            referencePoints.getValue());

    clientService.createClient(dto);
    NotificationUtils.success("Cliente empresarial creado exitosamente");
    close();
    clearForm();

    fireClientSavedEvent(dto);

    if (onSaveCallback != null) {
      onSaveCallback.run();
    }
  }

  private void saveUpdate() {
    if (!binderUpdate.isValid()) {
      NotificationUtils.error("Por favor, corrija los errores en el formulario");
      binderUpdate.validate();
      return;
    }

    ClientUpdateRequestDto dto =
        new ClientUpdateRequestDto(
            email.getValue(),
            null,
            null,
            phoneNumber.getValue(),
            null,
            null,
            null,
            null,
            rnc.getValue(),
            companyName.getValue(),
            preferredContactMethod.getValue(),
            emergencyContactName.getValue(),
            emergencyContactNumber.getValue(),
            rating.getValue(),
            creditLimit.getValue(),
            paymentTermsDays.getValue() != null ? paymentTermsDays.getValue().intValue() : null,
            notes.getValue(),
            referenceSource.getValue(),
            province.getValue(),
            municipality.getValue(),
            sector.getValue(),
            streetAddress.getValue(),
            referencePoints.getValue());

    clientService.updateClient(currentClient.getId(), dto);
    NotificationUtils.success("Empresa actualizada exitosamente");
    close();

    if (onSaveCallback != null) {
      onSaveCallback.run();
    }
  }

  private void clearForm() {
    isEditMode = false;
    currentClient = null;
    setHeaderTitle("Nueva Empresa");

    validationBean = new ValidationBean();
    validationBean.setRating(ClientRating.BUENO);
    validationBean.setCreditLimit(0.0);
    validationBean.setPaymentTermsDays(0);

    binder.readBean(validationBean);

    rating.setValue(ClientRating.BUENO);
    creditLimit.setValue(0.0);
    paymentTermsDays.setValue(0.0);
  }

  public void openForNew() {
    clearForm();
    companyName.focus();
    open();
  }

  public void openForEdit(Client client) {
    isEditMode = true;
    currentClient = client;
    setHeaderTitle("Editar Empresa");

    String trimmedRnc = client.getRnc() != null ? client.getRnc().trim() : null;

    ClientUpdateRequestDto dto =
        new ClientUpdateRequestDto(
            client.getEmail(),
            null,
            null,
            client.getPhoneNumber(),
            null,
            null,
            null,
            null,
            trimmedRnc,
            client.getCompanyName(),
            client.getPreferredContactMethod(),
            client.getEmergencyContactName(),
            client.getEmergencyContactNumber(),
            client.getRating(),
            client.getCreditLimit(),
            client.getPaymentTermsDays(),
            client.getNotes(),
            client.getReferenceSource(),
            client.getProvince(),
            client.getMunicipality(),
            client.getSector(),
            client.getStreetAddress(),
            client.getReferencePoints());

    binderUpdate.readBean(dto);
    companyName.focus();
    open();
  }

  public void addClientSavedListener(Consumer<ClientCreateRequestDto> listener) {
    clientSavedListeners.add(listener);
  }

  public void addClientCancelledListener(Runnable listener) {
    clientCancelledListeners.add(listener);
  }

  private void fireClientSavedEvent(ClientCreateRequestDto dto) {
    clientSavedListeners.forEach(listener -> listener.accept(dto));
  }

  private void fireClientCancelledEvent() {
    clientCancelledListeners.forEach(Runnable::run);
  }

  @Getter
  @Setter
  public static class ValidationBean {
    private String companyName;
    private String rnc;
    private String email;
    private String phoneNumber;
    private PreferredContactMethod preferredContactMethod;
    private String emergencyContactName;
    private String emergencyContactNumber;
    private ClientRating rating;
    private ReferenceSource referenceSource;
    private String province;
    private String municipality;
    private String sector;
    private String streetAddress;
    private String referencePoints;
    private Double creditLimit;
    private Integer paymentTermsDays;
    private String notes;
  }
}
