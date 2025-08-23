package com.wornux.views.clients;

import static com.wornux.constants.ValidationConstants.*;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
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
public class IndividualClientForm extends Dialog {

  private final TextField firstName = new TextField("Nombre");
  private final TextField lastName = new TextField("Apellido");
  private final EmailField email = new EmailField("Correo Electrónico");
  private final TextField phoneNumber = new TextField("Teléfono");
  private final DatePicker birthDate = new DatePicker("Fecha de Nacimiento");
  private final TextField nationality = new TextField("Nacionalidad");

  private final TextField cedula = new TextField("Cédula");
  private final TextField passport = new TextField("Pasaporte");

  private final ComboBox<PreferredContactMethod> preferredContactMethod =
      new ComboBox<>("Método de Contacto Preferido");
  private final TextField emergencyContactName = new TextField("Nombre del Contacto de Emergencia");
  private final TextField emergencyContactNumber = new TextField("Teléfono de Emergencia");

  private final TextField province = new TextField("Provincia");
  private final TextField municipality = new TextField("Municipio");
  private final TextField sector = new TextField("Sector");
  private final TextField streetAddress = new TextField("Dirección");
  private final TextArea referencePoints = new TextArea("Puntos de Referencia");

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

  public IndividualClientForm(ClientService clientService) {
    this.clientService = clientService;

    setHeaderTitle("Nuevo Cliente Individual");
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
    setupDynamicFieldValidation();
    setupEventListeners();
  }

  private void createForm() {
    FormLayout personalInfo = new FormLayout();
    personalInfo.add(firstName, lastName, email, phoneNumber, birthDate, nationality);
    personalInfo.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

    FormLayout identificationInfo = new FormLayout();
    identificationInfo.add(cedula, passport);
    identificationInfo.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

    FormLayout contactInfo = new FormLayout();
    contactInfo.add(preferredContactMethod, emergencyContactName, emergencyContactNumber);
    contactInfo.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

    FormLayout addressInfo = new FormLayout();
    addressInfo.add(province, municipality, sector, streetAddress);
    addressInfo.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

    FormLayout additionalInfo = new FormLayout();
    additionalInfo.add(rating, referenceSource);
    additionalInfo.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

    preferredContactMethod.setItems(PreferredContactMethod.values());
    preferredContactMethod.setItemLabelGenerator(PreferredContactMethod::name);

    rating.setItems(ClientRating.values());
    rating.setItemLabelGenerator(ClientRating::name);
    rating.setValue(ClientRating.BUENO);

    referenceSource.setItems(ReferenceSource.values());
    referenceSource.setItemLabelGenerator(ReferenceSource::name);

    referencePoints.setMaxLength(500);
    notes.setMaxLength(1000);

    nationality.setValue("Dominicana");

    configureFieldsForRealTimeValidation();

    addIconsToFields();

    VerticalLayout content = new VerticalLayout();
    content.add(
        new H3("Información Personal"),
        personalInfo,
        new H3("Identificación"),
        identificationInfo,
        new H3("Información de Contacto"),
        contactInfo,
        new H3("Dirección"),
        addressInfo,
        referencePoints,
        new H3("Información Adicional"),
        additionalInfo,
        notes);

    content.addClassNames(LumoUtility.Padding.MEDIUM);

    HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
    buttonLayout.addClassNames(LumoUtility.JustifyContent.END, LumoUtility.Gap.MEDIUM);

    add(content, buttonLayout);
  }

  private void configureFieldsForRealTimeValidation() {
    firstName.setValueChangeMode(ValueChangeMode.EAGER);
    lastName.setValueChangeMode(ValueChangeMode.EAGER);
    email.setValueChangeMode(ValueChangeMode.EAGER);
    phoneNumber.setValueChangeMode(ValueChangeMode.EAGER);
    nationality.setValueChangeMode(ValueChangeMode.EAGER);
    cedula.setValueChangeMode(ValueChangeMode.EAGER);
    passport.setValueChangeMode(ValueChangeMode.EAGER);
    emergencyContactName.setValueChangeMode(ValueChangeMode.EAGER);
    emergencyContactNumber.setValueChangeMode(ValueChangeMode.EAGER);
    province.setValueChangeMode(ValueChangeMode.EAGER);
    municipality.setValueChangeMode(ValueChangeMode.EAGER);
    sector.setValueChangeMode(ValueChangeMode.EAGER);
    streetAddress.setValueChangeMode(ValueChangeMode.EAGER);
    referencePoints.setValueChangeMode(ValueChangeMode.EAGER);
    notes.setValueChangeMode(ValueChangeMode.EAGER);

    firstName.setRequiredIndicatorVisible(true);
    lastName.setRequiredIndicatorVisible(true);
    email.setRequiredIndicatorVisible(true);
    phoneNumber.setRequiredIndicatorVisible(true);
    nationality.setRequiredIndicatorVisible(true);
    province.setRequiredIndicatorVisible(true);
    municipality.setRequiredIndicatorVisible(true);
    sector.setRequiredIndicatorVisible(true);
    streetAddress.setRequiredIndicatorVisible(true);

    cedula.setPlaceholder("Ej: 40212345678");
    cedula.setHelperText("11 dígitos");
    passport.setPlaceholder("Ej: A12345678");
    passport.setHelperText("Formato internacional");

    email.setErrorMessage("Proporcione un correo electrónico válido");
    province.setErrorMessage("La provincia es requerida");
    municipality.setErrorMessage("El municipio es requerido");
    sector.setErrorMessage("El sector es requerido");
    streetAddress.setErrorMessage("La dirección es requerida");
  }

  private void addIconsToFields() {
    firstName.setPrefixComponent(VaadinIcon.USER.create());
    lastName.setPrefixComponent(VaadinIcon.USER.create());
    email.setPrefixComponent(VaadinIcon.ENVELOPE.create());
    phoneNumber.setPrefixComponent(VaadinIcon.PHONE.create());
    birthDate.setPrefixComponent(VaadinIcon.CALENDAR.create());
    nationality.setPrefixComponent(VaadinIcon.FLAG.create());
    cedula.setPrefixComponent(VaadinIcon.CREDIT_CARD.create());
    passport.setPrefixComponent(VaadinIcon.AIRPLANE.create());
    preferredContactMethod.setPrefixComponent(VaadinIcon.CONNECT.create());
    emergencyContactName.setPrefixComponent(VaadinIcon.USERS.create());
    emergencyContactNumber.setPrefixComponent(VaadinIcon.PHONE_LANDLINE.create());
    province.setPrefixComponent(VaadinIcon.LOCATION_ARROW.create());
    municipality.setPrefixComponent(VaadinIcon.HOME.create());
    sector.setPrefixComponent(VaadinIcon.MAP_MARKER.create());
    streetAddress.setPrefixComponent(VaadinIcon.ROAD.create());
    referencePoints.setPrefixComponent(VaadinIcon.INFO_CIRCLE.create());
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
        .forField(firstName)
        .asRequired("El nombre es requerido")
        .withValidator(
            new StringLengthValidator("El nombre debe tener al menos 2 caracteres", 2, null))
        .bind(ValidationBean::getFirstName, ValidationBean::setFirstName);

    binder
        .forField(lastName)
        .asRequired("El apellido es requerido")
        .withValidator(
            new StringLengthValidator("El apellido debe tener al menos 2 caracteres", 2, null))
        .bind(ValidationBean::getLastName, ValidationBean::setLastName);

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
        .forField(nationality)
        .asRequired("La nacionalidad es requerida")
        .withValidator(new StringLengthValidator("La nacionalidad no puede estar vacía", 1, null))
        .bind(ValidationBean::getNationality, ValidationBean::setNationality);

    binder
        .forField(cedula)
        .withValidator(this::validateCedulaConditional)
        .bind(ValidationBean::getCedula, ValidationBean::setCedula);

    binder
        .forField(passport)
        .withValidator(this::validatePassportConditional)
        .bind(ValidationBean::getPassport, ValidationBean::setPassport);

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

    binder.forField(birthDate).bind(ValidationBean::getBirthDate, ValidationBean::setBirthDate);

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
        .forField(notes)
        .withValidator(
            new StringLengthValidator("Las notas no pueden exceder 1000 caracteres", 0, 1000))
        .bind(ValidationBean::getNotes, ValidationBean::setNotes);
  }

  private void setupUpdateBinder() {
    binderUpdate
        .forField(firstName)
        .asRequired("El nombre es requerido")
        .withValidator(
            new StringLengthValidator("El nombre debe tener al menos 2 caracteres", 2, null))
        .bind(ClientUpdateRequestDto::getFirstName, ClientUpdateRequestDto::setFirstName);

    binderUpdate
        .forField(lastName)
        .asRequired("El apellido es requerido")
        .withValidator(
            new StringLengthValidator("El apellido debe tener al menos 2 caracteres", 2, null))
        .bind(ClientUpdateRequestDto::getLastName, ClientUpdateRequestDto::setLastName);

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
                "Proporcione un número de teléfono válido", DOMINICAN_PHONE_PATTERN))
        .bind(ClientUpdateRequestDto::getPhoneNumber, ClientUpdateRequestDto::setPhoneNumber);

    binderUpdate
        .forField(nationality)
        .asRequired("La nacionalidad es requerida")
        .withValidator(new StringLengthValidator("La nacionalidad no puede estar vacía", 1, null))
        .bind(ClientUpdateRequestDto::getNationality, ClientUpdateRequestDto::setNationality);

    binderUpdate
        .forField(cedula)
        .withValidator(this::validateCedulaConditional)
        .bind(ClientUpdateRequestDto::getCedula, ClientUpdateRequestDto::setCedula);

    binderUpdate
        .forField(passport)
        .withValidator(this::validatePassportConditional)
        .bind(ClientUpdateRequestDto::getPassport, ClientUpdateRequestDto::setPassport);

    binderUpdate
        .forField(emergencyContactNumber)
        .withValidator(this::validateEmergencyPhone)
        .bind(
            ClientUpdateRequestDto::getEmergencyContactNumber,
            ClientUpdateRequestDto::setEmergencyContactNumber);

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
        .forField(birthDate)
        .bind(ClientUpdateRequestDto::getBirthDate, ClientUpdateRequestDto::setBirthDate);

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
        .forField(rating)
        .bind(ClientUpdateRequestDto::getRating, ClientUpdateRequestDto::setRating);

    binderUpdate
        .forField(referenceSource)
        .bind(
            ClientUpdateRequestDto::getReferenceSource, ClientUpdateRequestDto::setReferenceSource);

    binderUpdate
        .forField(referencePoints)
        .withValidator(
            new StringLengthValidator(
                "Los puntos de referencia no pueden exceder 500 caracteres", 0, 500))
        .bind(
            ClientUpdateRequestDto::getReferencePoints, ClientUpdateRequestDto::setReferencePoints);

    binderUpdate
        .forField(notes)
        .withValidator(
            new StringLengthValidator("Las notas no pueden exceder 1000 caracteres", 0, 1000))
        .bind(ClientUpdateRequestDto::getNotes, ClientUpdateRequestDto::setNotes);
  }

  private ValidationResult validateCedulaConditional(String value, ValueContext context) {
    String cedulaValue = value != null ? value.trim() : "";
    String passportValue = passport.getValue() != null ? passport.getValue().trim() : "";

    if (cedulaValue.isEmpty() && passportValue.isEmpty()) {
      return ValidationResult.error("Debe proporcionar cédula o pasaporte");
    }

    if (!cedulaValue.isEmpty()) {
      if (!cedulaValue.matches(CEDULA_PATTERN)) {
        return ValidationResult.error("La cédula debe contener exactamente 11 dígitos");
      }
    }

    return ValidationResult.ok();
  }

  private ValidationResult validatePassportConditional(String value, ValueContext context) {
    String passportValue = value != null ? value.trim() : "";
    String cedulaValue = cedula.getValue() != null ? cedula.getValue().trim() : "";

    if (passportValue.isEmpty() && cedulaValue.isEmpty()) {
      return ValidationResult.error("Debe proporcionar cédula o pasaporte");
    }

    if (!passportValue.isEmpty()) {
      if (!passportValue.matches(PASSPORT_PATTERN)) {
        return ValidationResult.error("Formato de pasaporte inválido");
      }
    }

    return ValidationResult.ok();
  }

  private void setupEventListeners() {
    saveButton.addClickListener(this::save);
    cancelButton.addClickListener(
        e -> {
          fireClientCancelledEvent();
          close();
        });

    cedula.addValueChangeListener(
        e -> {
          // Re-validate both fields when cedula changes
          binder.validate();
          binderUpdate.validate();
        });

    passport.addValueChangeListener(
        e -> {
          // Re-validate both fields when passport changes
          binder.validate();
          binderUpdate.validate();
        });

    email.addValueChangeListener(e -> binder.validate());
    province.addValueChangeListener(e -> binder.validate());
    municipality.addValueChangeListener(e -> binder.validate());
    sector.addValueChangeListener(e -> binder.validate());
    streetAddress.addValueChangeListener(e -> binder.validate());
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

  private void setupDynamicFieldValidation() {
    cedula.addValueChangeListener(
        e -> {
          String cedulaValue = e.getValue();
          if (cedulaValue != null && !cedulaValue.trim().isEmpty()) {
            passport.setEnabled(false);
            passport.clear();
            passport.setRequiredIndicatorVisible(false);
          } else {
            passport.setEnabled(true);
            passport.setRequiredIndicatorVisible(true);
          }
        });

    passport.addValueChangeListener(
        e -> {
          String passportValue = e.getValue();
          if (passportValue != null && !passportValue.trim().isEmpty()) {
            cedula.setEnabled(false);
            cedula.clear();
            cedula.setRequiredIndicatorVisible(false);
          } else {
            cedula.setEnabled(true);
            cedula.setRequiredIndicatorVisible(true);
          }
        });
  }

  private void save(ClickEvent<Button> event) {
    try {
      if (isEditMode) {
        saveUpdate();
      } else {
        saveNew();
      }
    } catch (Exception e) {
      log.error("Error saving individual client", e);
      NotificationUtils.error("Error al guardar cliente: " + e.getMessage());
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
            firstName.getValue(),
            lastName.getValue(),
            phoneNumber.getValue(),
            birthDate.getValue(),
            nationality.getValue(),
            convertEmptyToNull(cedula.getValue()),
            convertEmptyToNull(passport.getValue()),
            null,
            null,
            preferredContactMethod.getValue(),
            convertEmptyToNull(emergencyContactName.getValue()),
            convertEmptyToNull(emergencyContactNumber.getValue()),
            rating.getValue(),
            null,
            null,
            convertEmptyToNull(notes.getValue()),
            referenceSource.getValue(),
            province.getValue(),
            municipality.getValue(),
            sector.getValue(),
            streetAddress.getValue(),
            convertEmptyToNull(referencePoints.getValue()));

    fireClientSavedEvent(dto);

    if (onSaveCallback != null) {
      onSaveCallback.run();
    }
    close();
  }

  private String convertEmptyToNull(String value) {
    return (value == null || value.trim().isEmpty()) ? null : value;
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
            firstName.getValue(),
            lastName.getValue(),
            phoneNumber.getValue(),
            birthDate.getValue(),
            nationality.getValue(),
            convertEmptyToNull(cedula.getValue()),
            convertEmptyToNull(passport.getValue()),
            null,
            null,
            preferredContactMethod.getValue(),
            convertEmptyToNull(emergencyContactName.getValue()),
            convertEmptyToNull(emergencyContactNumber.getValue()),
            rating.getValue(),
            null,
            null,
            convertEmptyToNull(notes.getValue()),
            referenceSource.getValue(),
            province.getValue(),
            municipality.getValue(),
            sector.getValue(),
            streetAddress.getValue(),
            convertEmptyToNull(referencePoints.getValue()));

    clientService.updateClient(currentClient.getId(), dto);
    NotificationUtils.success("Cliente actualizado exitosamente");

    if (onSaveCallback != null) {
      onSaveCallback.run();
    }
    close();
  }

  public void openForNew() {
    isEditMode = false;
    currentClient = null;
    setHeaderTitle("Nuevo Cliente Individual");

    validationBean = new ValidationBean();
    validationBean.setRating(ClientRating.BUENO);
    validationBean.setNationality("Dominicana");

    binder.readBean(validationBean);

    rating.setValue(ClientRating.BUENO);
    nationality.setValue("Dominicana");
    cedula.setEnabled(true);
    passport.setEnabled(true);
    cedula.setRequiredIndicatorVisible(true);
    passport.setRequiredIndicatorVisible(true);

    firstName.focus();
    open();
  }

  public void openForEdit(Client client) {
    isEditMode = true;
    currentClient = client;
    setHeaderTitle("Editar Cliente Individual");

    ClientUpdateRequestDto dto =
        new ClientUpdateRequestDto(
            client.getEmail(),
            client.getFirstName(),
            client.getLastName(),
            client.getPhoneNumber(),
            client.getBirthDate(),
            client.getNationality(),
            client.getCedula(),
            client.getPassport(),
            null,
            null,
            client.getPreferredContactMethod(),
            client.getEmergencyContactName(),
            client.getEmergencyContactNumber(),
            client.getRating(),
            null,
            null,
            client.getNotes(),
            client.getReferenceSource(),
            client.getProvince(),
            client.getMunicipality(),
            client.getSector(),
            client.getStreetAddress(),
            client.getReferencePoints());

    binderUpdate.readBean(dto);
    firstName.focus();
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
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private java.time.LocalDate birthDate;
    private String nationality;
    private String cedula;
    private String passport;
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
    private String notes;
  }
}
