package com.wornux.views.employees;

import static com.wornux.constants.ValidationConstants.DOMINICAN_PHONE_PATTERN;
import static com.wornux.constants.ValidationConstants.EMAIL_PATTERN;
import static com.wornux.constants.ValidationConstants.MAX_USERNAME_LENGTH;
import static com.wornux.constants.ValidationConstants.MIN_PASSWORD_LENGTH;
import static com.wornux.constants.ValidationConstants.MIN_USERNAME_LENGTH;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.enums.EmployeeRole;
import com.wornux.data.enums.Gender;
import com.wornux.dto.request.EmployeeCreateRequestDto;
import com.wornux.services.interfaces.EmployeeService;
import com.wornux.utils.NotificationUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmployeeForm extends Dialog {

  private static final String RESPONSIVE_STEP_WIDTH = "500px";
  // User Information
  private final TextField username = new TextField("Usuario");
  private final PasswordField password = new PasswordField("Contraseña");
  private final TextField firstName = new TextField("Nombre");
  private final TextField lastName = new TextField("Apellido");
  private final EmailField email = new EmailField("Correo Electrónico");
  private final TextField phoneNumber = new TextField("Teléfono");
  private final DatePicker birthDate = new DatePicker("Fecha de Nacimiento");
  private final ComboBox<Gender> gender = new ComboBox<>("Género");
  private final TextField nationality = new TextField("Nacionalidad");
  // Address Information
  private final TextField province = new TextField("Provincia");
  private final TextField municipality = new TextField("Municipio");
  private final TextField sector = new TextField("Sector");
  private final TextField streetAddress = new TextField("Dirección");
  // Employee Information
  private final ComboBox<EmployeeRole> employeeRole = new ComboBox<>("Rol");
  private final NumberField salary = new NumberField("Salario");
  private final DatePicker hireDate = new DatePicker("Fecha de Contratación");
  private final TextField workSchedule = new TextField("Horario Laboral");
  // Emergency Contact
  private final TextField emergencyContactName = new TextField("Nombre de Contacto de Emergencia");
  private final TextField emergencyContactPhone =
      new TextField("Teléfono de Contacto de Emergencia");
  private final Button saveButton = new Button("Guardar");
  private final Button cancelButton = new Button("Cancelar");
  private final Binder<EmployeeCreateRequestDto> binder =
      new BeanValidationBinder<>(EmployeeCreateRequestDto.class);
  private final List<Consumer<EmployeeCreateRequestDto>> employeeSavedListeners = new ArrayList<>();
  private final List<Runnable> employeeCancelledListeners = new ArrayList<>();
  private final transient EmployeeService employeeService;
  @Setter private transient Runnable onSaveCallback;

  public EmployeeForm(EmployeeService employeeService) {
    this.employeeService = employeeService;

    setHeaderTitle("Nuevo Empleado");
    setModal(true);
    setWidth("900px");
    setHeight("80vh");

    createForm();
    setupValidation();
    setupEventListeners();
  }

  private void createForm() {
    FormLayout userInfo = new FormLayout();
    userInfo.add(
        username,
        password,
        firstName,
        lastName,
        email,
        phoneNumber,
        birthDate,
        gender,
        nationality);
    userInfo.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1),
        new FormLayout.ResponsiveStep(RESPONSIVE_STEP_WIDTH, 2));

    FormLayout addressInfo = new FormLayout();
    addressInfo.add(province, municipality, sector, streetAddress);
    addressInfo.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1),
        new FormLayout.ResponsiveStep(RESPONSIVE_STEP_WIDTH, 2));

    FormLayout employeeInfo = new FormLayout();
    employeeInfo.add(employeeRole, salary, hireDate, workSchedule);
    employeeInfo.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1),
        new FormLayout.ResponsiveStep(RESPONSIVE_STEP_WIDTH, 2));

    FormLayout emergencyInfo = new FormLayout();
    emergencyInfo.add(emergencyContactName, emergencyContactPhone);
    emergencyInfo.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1),
        new FormLayout.ResponsiveStep(RESPONSIVE_STEP_WIDTH, 2));

    // Setup combo boxes
    gender.setItems(Gender.values());
    gender.setItemLabelGenerator(Gender::name);

    employeeRole.setItems(EmployeeRole.values());
    employeeRole.setItemLabelGenerator(EmployeeRole::getDisplayName);

    // Configure number fields
    salary.setMin(0);
    salary.setValue(0.0);
    salary.setSuffixComponent(new TextField().getPrefixComponent());

    // Configure text fields
    username.setPlaceholder("Ej: john.doe");

    phoneNumber.setPlaceholder("Ej: 809-555-5555");

    VerticalLayout content = new VerticalLayout();
    content.add(
        new H3("Información del Usuario"),
        userInfo,
        new H3("Dirección"),
        addressInfo,
        new H3("Información del Empleado"),
        employeeInfo,
        new H3("Contacto de Emergencia"),
        emergencyInfo);

    content.addClassNames(LumoUtility.Padding.MEDIUM);

    // Configure buttons
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

    HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
    buttonLayout.addClassNames(LumoUtility.JustifyContent.END, LumoUtility.Gap.MEDIUM);

    add(content, buttonLayout);
  }

  private void setupValidation() {
    // Setup basic field requirements without binding
    username.setRequired(true);
    username.setRequiredIndicatorVisible(true);
    username.setMinLength(MIN_USERNAME_LENGTH);
    username.setMaxLength(MAX_USERNAME_LENGTH);
    username.setErrorMessage(
        "El nombre de usario es requerido y debe tener entre "
            + MIN_USERNAME_LENGTH
            + " y "
            + MAX_USERNAME_LENGTH
            + " caracteres");

    password.setRequired(true);
    password.setRequiredIndicatorVisible(true);
    password.setMinLength(MIN_PASSWORD_LENGTH);
    password.setErrorMessage(
        "La contraseña es requerida y debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres");

    firstName.setRequired(true);
    firstName.setRequiredIndicatorVisible(true);
    firstName.setErrorMessage("El nombre es requerido");

    lastName.setRequired(true);
    lastName.setRequiredIndicatorVisible(true);
    lastName.setErrorMessage("El apellido es requerido");

    email.setRequired(true);
    email.setRequiredIndicatorVisible(true);
    email.setPattern(EMAIL_PATTERN);
    email.setErrorMessage("El correo electrónico es requerido y debe ser válido");

    phoneNumber.setRequired(true);
    phoneNumber.setRequiredIndicatorVisible(true);
    phoneNumber.setPattern(DOMINICAN_PHONE_PATTERN);
    phoneNumber.setErrorMessage("El teléfono debe ser en formato dominicano");

    birthDate.setRequired(true);
    birthDate.setRequiredIndicatorVisible(true);
    birthDate.setErrorMessage("La fecha de nacimiento es requerida");

    gender.setRequired(true);
    gender.setRequiredIndicatorVisible(true);
    gender.setErrorMessage("El género es requerido");

    sector.setRequired(true);
    sector.setRequiredIndicatorVisible(true);
    sector.setErrorMessage("El sector es requerido");

    streetAddress.setRequired(true);
    streetAddress.setRequiredIndicatorVisible(true);
    streetAddress.setErrorMessage("La dirección es requerida");

    employeeRole.setRequired(true);
    employeeRole.setRequiredIndicatorVisible(true);
    employeeRole.setErrorMessage("El rol es requerido");

    salary.setRequired(true);
    salary.setRequiredIndicatorVisible(true);
    salary.setErrorMessage("El salario es requerido");

    hireDate.setRequired(true);
    hireDate.setRequiredIndicatorVisible(true);
    hireDate.setErrorMessage("La fecha de contratación es requerida");

    workSchedule.setRequired(true);
    workSchedule.setRequiredIndicatorVisible(true);
    workSchedule.setErrorMessage("El horario laboral es requerido");
  }

  private void setupEventListeners() {
    saveButton.addClickListener(this::save);
    cancelButton.addClickListener(
        e -> {
          fireEmployeeCancelledEvent();
          close();
        });
  }

  private void save(ClickEvent<Button> event) {
    try {
      // Manual validation
      if (!validateForm()) {
        NotificationUtils.error("Por favor, complete todos los campos requeridos");
        return;
      }

      EmployeeCreateRequestDto dto =
          EmployeeCreateRequestDto.builder()
              .username(username.getValue())
              .password(password.getValue())
              .firstName(firstName.getValue())
              .lastName(lastName.getValue())
              .email(email.getValue())
              .phoneNumber(phoneNumber.getValue())
              .birthDate(birthDate.getValue())
              .gender(gender.getValue())
              .nationality(nationality.getValue())
              .province(province.getValue())
              .municipality(municipality.getValue())
              .sector(sector.getValue())
              .streetAddress(streetAddress.getValue())
              .employeeRole(employeeRole.getValue())
              .salary(salary.getValue())
              .hireDate(hireDate.getValue())
              .workSchedule(workSchedule.getValue())
              .emergencyContactName(emergencyContactName.getValue())
              .emergencyContactPhone(emergencyContactPhone.getValue())
              .build();

      employeeService.save(dto);
      NotificationUtils.success("Empleado creado exitosamente");

      // Fire the event with the created DTO
      fireEmployeeSavedEvent(dto);

      if (onSaveCallback != null) {
        onSaveCallback.run();
      }

      close();
    } catch (Exception e) {
      log.error("Error creating employee", e);
      NotificationUtils.error("Error al crear empleado: " + e.getMessage());
    }
  }

  /**
   * Validates all required form fields
   *
   * @return true if all required fields are valid, false otherwise
   */
  private boolean validateForm() {
    boolean isValid = true;

    // Validate required fields
    if (username.isEmpty()
        || username.getValue().length() < MIN_USERNAME_LENGTH
        || username.getValue().length() > MAX_USERNAME_LENGTH) {
      username.setInvalid(true);
      isValid = false;
    } else {
      username.setInvalid(false);
    }

    if (password.isEmpty() || password.getValue().length() < MIN_PASSWORD_LENGTH) {
      password.setInvalid(true);
      isValid = false;
    } else {
      password.setInvalid(false);
    }

    if (firstName.isEmpty()) {
      firstName.setInvalid(true);
      isValid = false;
    } else {
      firstName.setInvalid(false);
    }

    if (lastName.isEmpty()) {
      lastName.setInvalid(true);
      isValid = false;
    } else {
      lastName.setInvalid(false);
    }

    if (email.isEmpty() || !email.getValue().contains("@")) {
      email.setInvalid(true);
      isValid = false;
    } else {
      email.setInvalid(false);
    }

    if (phoneNumber.isEmpty()) {
      phoneNumber.setInvalid(true);
      isValid = false;
    } else {
      phoneNumber.setInvalid(false);
    }

    if (birthDate.isEmpty()) {
      birthDate.setInvalid(true);
      isValid = false;
    } else {
      birthDate.setInvalid(false);
    }

    if (gender.isEmpty()) {
      gender.setInvalid(true);
      isValid = false;
    } else {
      gender.setInvalid(false);
    }

    if (province.isEmpty()) {
      province.setInvalid(true);
      isValid = false;
    } else {
      province.setInvalid(false);
    }

    if (municipality.isEmpty()) {
      municipality.setInvalid(true);
      isValid = false;
    } else {
      municipality.setInvalid(false);
    }

    if (sector.isEmpty()) {
      sector.setInvalid(true);
      isValid = false;
    } else {
      sector.setInvalid(false);
    }

    if (streetAddress.isEmpty()) {
      streetAddress.setInvalid(true);
      isValid = false;
    } else {
      streetAddress.setInvalid(false);
    }

    if (employeeRole.isEmpty()) {
      employeeRole.setInvalid(true);
      isValid = false;
    } else {
      employeeRole.setInvalid(false);
    }

    if (salary.isEmpty() || salary.getValue() < 0) {
      salary.setInvalid(true);
      isValid = false;
    } else {
      salary.setInvalid(false);
    }

    if (hireDate.isEmpty()) {
      hireDate.setInvalid(true);
      isValid = false;
    } else {
      hireDate.setInvalid(false);
    }

    if (workSchedule.isEmpty()) {
      workSchedule.setInvalid(true);
      isValid = false;
    } else {
      workSchedule.setInvalid(false);
    }

    return isValid;
  }

  public void openForNew() {
    // Clear all fields and reset form
    clearForm();
    // Enable all fields explicitly
    enableAllFields();
    // Reset any invalid states
    resetValidationStates();
    // Set focus on first field
    username.focus();
    open();
  }

  /** Clears all form fields */
  private void clearForm() {
    username.clear();
    password.clear();
    firstName.clear();
    lastName.clear();
    email.clear();
    phoneNumber.clear();
    birthDate.clear();
    gender.clear();
    nationality.clear();
    province.clear();
    municipality.clear();
    sector.clear();
    streetAddress.clear();
    employeeRole.clear();
    salary.setValue(0.0);
    hireDate.clear();
    workSchedule.clear();
    emergencyContactName.clear();
    emergencyContactPhone.clear();
  }

  /** Enables all form fields for data entry */
  private void enableAllFields() {
    username.setEnabled(true);
    password.setEnabled(true);
    firstName.setEnabled(true);
    lastName.setEnabled(true);
    email.setEnabled(true);
    phoneNumber.setEnabled(true);
    birthDate.setEnabled(true);
    gender.setEnabled(true);
    nationality.setEnabled(true);
    province.setEnabled(true);
    municipality.setEnabled(true);
    sector.setEnabled(true);
    streetAddress.setEnabled(true);
    employeeRole.setEnabled(true);
    salary.setEnabled(true);
    hireDate.setEnabled(true);
    workSchedule.setEnabled(true);
    emergencyContactName.setEnabled(true);
    emergencyContactPhone.setEnabled(true);
    saveButton.setEnabled(true);
  }

  /** Resets validation states for all fields */
  private void resetValidationStates() {
    username.setInvalid(false);
    password.setInvalid(false);
    firstName.setInvalid(false);
    lastName.setInvalid(false);
    email.setInvalid(false);
    phoneNumber.setInvalid(false);
    birthDate.setInvalid(false);
    gender.setInvalid(false);
    province.setInvalid(false);
    municipality.setInvalid(false);
    sector.setInvalid(false);
    streetAddress.setInvalid(false);
    employeeRole.setInvalid(false);
    salary.setInvalid(false);
    hireDate.setInvalid(false);
    workSchedule.setInvalid(false);
  }

  /**
   * Adds a listener that will be called when an employee is successfully saved.
   *
   * @param listener Consumer that receives the saved employee DTO
   */
  public void addEmployeeSavedListener(Consumer<EmployeeCreateRequestDto> listener) {
    employeeSavedListeners.add(listener);
  }

  /**
   * Adds a listener that will be called when the form is cancelled.
   *
   * @param listener Runnable to execute on cancel
   */
  public void addEmployeeCancelledListener(Runnable listener) {
    employeeCancelledListeners.add(listener);
  }

  /**
   * Notifies all saved listeners that an employee was successfully saved.
   *
   * @param dto The saved employee DTO
   */
  private void fireEmployeeSavedEvent(EmployeeCreateRequestDto dto) {
    employeeSavedListeners.forEach(listener -> listener.accept(dto));
  }

  /** Notifies all cancelled listeners that the form was cancelled. */
  private void fireEmployeeCancelledEvent() {
    employeeCancelledListeners.forEach(Runnable::run);
  }
}
