package com.wornux.views.employees;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.constants.ValidationConstants;
import com.wornux.data.entity.Employee;
import com.wornux.data.enums.EmployeeRole;
import com.wornux.data.enums.Gender;
import com.wornux.dto.request.EmployeeCreateRequestDto;
import com.wornux.dto.request.EmployeeUpdateRequestDto;
import com.wornux.exception.DuplicateEmployeeException;
import com.wornux.services.interfaces.EmployeeService;
import com.wornux.utils.NotificationUtils;
import jakarta.validation.ValidationException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.wornux.constants.ValidationConstants.*;

@Slf4j
public class EmployeeForm extends Dialog {

    // Form Components
    private final TextField username = new TextField("Usuario");
    private final PasswordField password = new PasswordField("Contraseña");
    private final TextField firstName = new TextField("Nombre");
    private final TextField lastName = new TextField("Apellido");
    private final EmailField email = new EmailField("Correo Electrónico");
    private final TextField phoneNumber = new TextField("Teléfono");
    private final DatePicker birthDate = new DatePicker("Fecha de Nacimiento");
    private final ComboBox<Gender> gender = new ComboBox<>("Género");
    private final TextField nationality = new TextField("Nacionalidad");
    private final TextField province = new TextField("Provincia");
    private final TextField municipality = new TextField("Municipio");
    private final TextField sector = new TextField("Sector");
    private final TextField streetAddress = new TextField("Dirección");
    private final ComboBox<EmployeeRole> employeeRole = new ComboBox<>("Rol de Empleado");
    private final NumberField salary = new NumberField("Salario");
    private final DatePicker hireDate = new DatePicker("Fecha de Contratación");
    private final TextField workSchedule = new TextField("Horario de Trabajo");
    private final TextField emergencyContactName = new TextField("Nombre Contacto de Emergencia");
    private final TextField emergencyContactPhone = new TextField("Teléfono Contacto de Emergencia");

    // Form buttons
    private final Button saveButton = new Button("Guardar");
    private final Button cancelButton = new Button("Cancelar");

    // Header
    private final H3 headerTitle = new H3();

    // Binders
    private final Binder<ValidationBean> binder = new Binder<>(ValidationBean.class);
    private final Binder<EmployeeUpdateRequestDto> binderUpdate = new Binder<>(EmployeeUpdateRequestDto.class);

    // Services
    private final transient EmployeeService employeeService;

    // State
    private boolean isEditMode = false;
    private Employee currentEmployee;
    private ValidationBean validationBean;
    private Runnable onSaveCallback;

    // Event listeners
    private final List<Consumer<EmployeeCreateRequestDto>> employeeSavedListeners = new ArrayList<>();
    private final List<Runnable> employeeCancelledListeners = new ArrayList<>();

    public EmployeeForm(EmployeeService employeeService) {
        this.employeeService = employeeService;
        this.validationBean = new ValidationBean();

        setupDialog();
        setupForm();
        setupBinders();
        setupEventListeners();
        configureFieldValidation();
    }

    private void setupDialog() {
        setModal(true);
        setDraggable(false);
        setResizable(false);
        setWidth("800px");
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
        username.setPrefixComponent(VaadinIcon.USER.create());
        username.setRequiredIndicatorVisible(true);

        password.setPrefixComponent(VaadinIcon.LOCK.create());
        password.setRequiredIndicatorVisible(true);

        email.setPrefixComponent(VaadinIcon.ENVELOPE.create());
        email.setRequiredIndicatorVisible(true);

        firstName.setPrefixComponent(VaadinIcon.USER.create());
        firstName.setRequiredIndicatorVisible(true);

        lastName.setPrefixComponent(VaadinIcon.USER.create());
        lastName.setRequiredIndicatorVisible(true);

        phoneNumber.setPrefixComponent(VaadinIcon.PHONE.create());

        province.setRequiredIndicatorVisible(true);
        municipality.setRequiredIndicatorVisible(true);
        sector.setRequiredIndicatorVisible(true);
        streetAddress.setRequiredIndicatorVisible(true);

        employeeRole.setItems(EmployeeRole.values());
        employeeRole.setItemLabelGenerator(EmployeeRole::getDisplayName);
        employeeRole.setRequiredIndicatorVisible(true);

        gender.setItems(Gender.values());
        gender.setItemLabelGenerator(Gender::name);

        salary.setPrefixComponent(VaadinIcon.DOLLAR.create());
        salary.setRequiredIndicatorVisible(true);
        salary.setMin(0);

        hireDate.setRequiredIndicatorVisible(true);
        workSchedule.setRequiredIndicatorVisible(true);

        emergencyContactPhone.setPrefixComponent(VaadinIcon.PHONE.create());

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.addClassNames(LumoUtility.Padding.MEDIUM);

        formLayout.add(
                username, password,
                firstName, lastName,
                email, phoneNumber,
                birthDate, gender,
                nationality,
                province, municipality,
                sector, streetAddress,
                employeeRole, salary,
                hireDate, workSchedule,
                emergencyContactName, emergencyContactPhone
        );

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        formLayout.setColspan(streetAddress, 2);
        formLayout.setColspan(workSchedule, 2);

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
        binder.forField(username)
                .withValidator(new StringLengthValidator("El usuario debe tener entre 3 y 50 caracteres", MIN_USERNAME_LENGTH, MAX_USERNAME_LENGTH))
                .bind(ValidationBean::getUsername, ValidationBean::setUsername);

        binder.forField(password)
                .withValidator(new StringLengthValidator("La contraseña debe tener al menos 8 caracteres", MIN_PASSWORD_LENGTH, null))
                .bind(ValidationBean::getPassword, ValidationBean::setPassword);

        binder.forField(firstName)
                .withValidator(new StringLengthValidator("El nombre debe tener al menos 2 caracteres", 2, null))
                .bind(ValidationBean::getFirstName, ValidationBean::setFirstName);

        binder.forField(lastName)
                .withValidator(new StringLengthValidator("El apellido debe tener al menos 2 caracteres", 2, null))
                .bind(ValidationBean::getLastName, ValidationBean::setLastName);

        binder.forField(email)
                .withValidator(new EmailValidator("Proporcione un correo electrónico válido"))
                .bind(ValidationBean::getEmail, ValidationBean::setEmail);

        binder.forField(phoneNumber)
                .withValidator(new RegexpValidator("Proporcione un número de teléfono válido", DOMINICAN_PHONE_PATTERN, true))
                .bind(ValidationBean::getPhoneNumber, ValidationBean::setPhoneNumber);

        binder.forField(birthDate)
                .withValidator(this::validateBirthDate)
                .bind(ValidationBean::getBirthDate, ValidationBean::setBirthDate);

        binder.forField(gender)
                .bind(ValidationBean::getGender, ValidationBean::setGender);

        binder.forField(nationality)
                .bind(ValidationBean::getNationality, ValidationBean::setNationality);

        binder.forField(province)
                .asRequired("La provincia es requerida")
                .bind(ValidationBean::getProvince, ValidationBean::setProvince);

        binder.forField(municipality)
                .asRequired("El municipio es requerido")
                .bind(ValidationBean::getMunicipality, ValidationBean::setMunicipality);

        binder.forField(sector)
                .asRequired("El sector es requerido")
                .bind(ValidationBean::getSector, ValidationBean::setSector);

        binder.forField(streetAddress)
                .asRequired("La dirección es requerida")
                .bind(ValidationBean::getStreetAddress, ValidationBean::setStreetAddress);

        binder.forField(employeeRole)
                .asRequired("El rol de empleado es requerido")
                .bind(ValidationBean::getEmployeeRole, ValidationBean::setEmployeeRole);

        binder.forField(salary)
                .withValidator(this::validateSalary)
                .bind(ValidationBean::getSalary, ValidationBean::setSalary);

        binder.forField(hireDate)
                .asRequired("La fecha de contratación es requerida")
                .bind(ValidationBean::getHireDate, ValidationBean::setHireDate);

        binder.forField(workSchedule)
                .asRequired("El horario de trabajo es requerido")
                .bind(ValidationBean::getWorkSchedule, ValidationBean::setWorkSchedule);

        binder.forField(emergencyContactName)
                .bind(ValidationBean::getEmergencyContactName, ValidationBean::setEmergencyContactName);

        binder.forField(emergencyContactPhone)
                .bind(ValidationBean::getEmergencyContactPhone, ValidationBean::setEmergencyContactPhone);
    }

    private void setupUpdateBinder() {
        binderUpdate.forField(username)
                .withValidator(new StringLengthValidator("El usuario debe tener entre 3 y 50 caracteres", MIN_USERNAME_LENGTH, MAX_USERNAME_LENGTH))
                .bind(EmployeeUpdateRequestDto::getUsername, EmployeeUpdateRequestDto::setUsername);

        binderUpdate.forField(firstName)
                .withValidator(new StringLengthValidator("El nombre debe tener al menos 2 caracteres", 2, null))
                .bind(EmployeeUpdateRequestDto::getFirstName, EmployeeUpdateRequestDto::setFirstName);

        binderUpdate.forField(lastName)
                .withValidator(new StringLengthValidator("El apellido debe tener al menos 2 caracteres", 2, null))
                .bind(EmployeeUpdateRequestDto::getLastName, EmployeeUpdateRequestDto::setLastName);

        binderUpdate.forField(email)
                .withValidator(new EmailValidator("Proporcione un correo electrónico válido"))
                .bind(EmployeeUpdateRequestDto::getEmail, EmployeeUpdateRequestDto::setEmail);

        binderUpdate.forField(phoneNumber)
                .withValidator(new RegexpValidator("Proporcione un número de teléfono válido", DOMINICAN_PHONE_PATTERN, true))
                .bind(EmployeeUpdateRequestDto::getPhoneNumber, EmployeeUpdateRequestDto::setPhoneNumber);

        binderUpdate.forField(birthDate)
                .withValidator(this::validateBirthDate)
                .bind(EmployeeUpdateRequestDto::getBirthDate, EmployeeUpdateRequestDto::setBirthDate);

        binderUpdate.forField(gender)
                .bind(EmployeeUpdateRequestDto::getGender, EmployeeUpdateRequestDto::setGender);

        binderUpdate.forField(nationality)
                .bind(EmployeeUpdateRequestDto::getNationality, EmployeeUpdateRequestDto::setNationality);

        binderUpdate.forField(province)
                .asRequired("La provincia es requerida")
                .bind(EmployeeUpdateRequestDto::getProvince, EmployeeUpdateRequestDto::setProvince);

        binderUpdate.forField(municipality)
                .asRequired("El municipio es requerido")
                .bind(EmployeeUpdateRequestDto::getMunicipality, EmployeeUpdateRequestDto::setMunicipality);

        binderUpdate.forField(sector)
                .asRequired("El sector es requerido")
                .bind(EmployeeUpdateRequestDto::getSector, EmployeeUpdateRequestDto::setSector);

        binderUpdate.forField(streetAddress)
                .asRequired("La dirección es requerida")
                .bind(EmployeeUpdateRequestDto::getStreetAddress, EmployeeUpdateRequestDto::setStreetAddress);

        binderUpdate.forField(employeeRole)
                .asRequired("El rol de empleado es requerido")
                .bind(EmployeeUpdateRequestDto::getEmployeeRole, EmployeeUpdateRequestDto::setEmployeeRole);

        binderUpdate.forField(salary)
                .withValidator(this::validateSalary)
                .bind(EmployeeUpdateRequestDto::getSalary, EmployeeUpdateRequestDto::setSalary);

        binderUpdate.forField(hireDate)
                .asRequired("La fecha de contratación es requerida")
                .bind(EmployeeUpdateRequestDto::getHireDate, EmployeeUpdateRequestDto::setHireDate);

        binderUpdate.forField(workSchedule)
                .asRequired("El horario de trabajo es requerido")
                .bind(EmployeeUpdateRequestDto::getWorkSchedule, EmployeeUpdateRequestDto::setWorkSchedule);

        binderUpdate.forField(emergencyContactName)
                .bind(EmployeeUpdateRequestDto::getEmergencyContactName, EmployeeUpdateRequestDto::setEmergencyContactName);

        binderUpdate.forField(emergencyContactPhone)
                .bind(EmployeeUpdateRequestDto::getEmergencyContactPhone, EmployeeUpdateRequestDto::setEmergencyContactPhone);
    }

    private ValidationResult validateBirthDate(LocalDate value, ValueContext context) {
        if (value != null && value.isAfter(LocalDate.now().minusYears(16))) {
            return ValidationResult.error("El empleado debe tener al menos 16 años");
        }
        return ValidationResult.ok();
    }

    private ValidationResult validateSalary(Double value, ValueContext context) {
        if (value == null) {
            return ValidationResult.error("El salario es requerido");
        }
        if (value < 0) {
            return ValidationResult.error("El salario no puede ser negativo");
        }
        return ValidationResult.ok();
    }

    private void setupEventListeners() {
        saveButton.addClickListener(this::save);
        cancelButton.addClickListener(e -> {
            close();
            fireEmployeeCancelledEvent();
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
        if (!binder.isValid()) {
            NotificationUtils.error("Por favor, corrija los errores en el formulario");
            return;
        }

        try {
            EmployeeCreateRequestDto dto = EmployeeCreateRequestDto.builder()
                    .username(validationBean.getUsername())
                    .password(validationBean.getPassword())
                    .firstName(validationBean.getFirstName())
                    .lastName(validationBean.getLastName())
                    .email(validationBean.getEmail())
                    .phoneNumber(validationBean.getPhoneNumber())
                    .birthDate(validationBean.getBirthDate())
                    .gender(validationBean.getGender())
                    .nationality(validationBean.getNationality())
                    .province(validationBean.getProvince())
                    .municipality(validationBean.getMunicipality())
                    .sector(validationBean.getSector())
                    .streetAddress(validationBean.getStreetAddress())
                    .employeeRole(validationBean.getEmployeeRole())
                    .salary(validationBean.getSalary())
                    .hireDate(validationBean.getHireDate())
                    .workSchedule(validationBean.getWorkSchedule())
                    .emergencyContactName(validationBean.getEmergencyContactName())
                    .emergencyContactPhone(validationBean.getEmergencyContactPhone())
                    .build();

            employeeService.save(dto);
            NotificationUtils.success("Empleado creado exitosamente");
            close();
            fireEmployeeSavedEvent(dto);
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
        } catch (Exception e) {
            log.error("Error saving employee", e);
            NotificationUtils.error("Error al guardar el empleado: " + e.getMessage());
        }
    }

    private void saveUpdate() {
        if (!binderUpdate.isValid()) {
            NotificationUtils.error("Por favor, corrija los errores en el formulario");
            return;
        }

        try {
            EmployeeUpdateRequestDto updateDto = binderUpdate.getBean();
            employeeService.updateEmployee(currentEmployee.getId(), updateDto);
            NotificationUtils.success("Empleado actualizado exitosamente");
            close();
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
        } catch (ValidationException e) {
            log.error("Validation error updating employee", e);
            NotificationUtils.error("Error de validación: " + e.getMessage());
        } catch (DuplicateEmployeeException e) {
            log.error("Duplicate employee error", e);
            NotificationUtils.error("Empleado duplicado: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error updating employee", e);
            NotificationUtils.error("Error al actualizar el empleado: " + e.getMessage());
        }
    }

    public void openForNew() {
        isEditMode = false;
        clearAllValidationErrors();
        currentEmployee = null;
        validationBean = new ValidationBean();

        updateHeaderTitle("Nuevo Empleado");
        clearForm();
        binder.readBean(validationBean);

        password.setVisible(true);
        password.setRequiredIndicatorVisible(true);
        hireDate.setValue(LocalDate.now());

        firstName.focus();
        open();
    }

    public void openForEdit(Employee employee) {
        clearAllValidationErrors();
        isEditMode = true;
        currentEmployee = employee;

        updateHeaderTitle("Editar Empleado: " + employee.getFirstName() + " " + employee.getLastName());

        password.setVisible(false);
        password.setRequiredIndicatorVisible(false);

        populateForm(employee);
        EmployeeUpdateRequestDto updateDto = createUpdateDtoFromEmployee(employee);
        binderUpdate.setBean(updateDto);

        username.focus();
        open();
    }

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
        salary.clear();
        hireDate.clear();
        workSchedule.clear();
        emergencyContactName.clear();
        emergencyContactPhone.clear();
    }

    private void populateForm(Employee employee) {
        username.setValue(employee.getUsername());
        firstName.setValue(employee.getFirstName());
        lastName.setValue(employee.getLastName());
        email.setValue(employee.getEmail() != null ? employee.getEmail() : "");
        phoneNumber.setValue(employee.getPhoneNumber() != null ? employee.getPhoneNumber() : "");
        birthDate.setValue(employee.getBirthDate());
        gender.setValue(employee.getGender());
        nationality.setValue(employee.getNationality() != null ? employee.getNationality() : "");
        province.setValue(employee.getProvince());
        municipality.setValue(employee.getMunicipality());
        sector.setValue(employee.getSector());
        streetAddress.setValue(employee.getStreetAddress());
        employeeRole.setValue(employee.getEmployeeRole());
        salary.setValue(employee.getSalary());
        hireDate.setValue(employee.getHireDate());
        workSchedule.setValue(employee.getWorkSchedule());
        emergencyContactName.setValue(employee.getEmergencyContactName() != null ? employee.getEmergencyContactName() : "");
        emergencyContactPhone.setValue(employee.getEmergencyContactPhone() != null ? employee.getEmergencyContactPhone() : "");
    }

    private void configureFieldValidation() {
        // Validación para campos requeridos
        Stream.of(firstName, lastName, username, email, phoneNumber, province, municipality, sector, streetAddress, workSchedule)
                .forEach(this::setupRequiredFieldValidation);

        // Validación específica para email
        email.addValueChangeListener(event -> validateEmailFormat(event.getValue()));

        // Validación específica para username
        username.addValueChangeListener(event -> validateUsernameLength(event.getValue()));

        // Validación específica para teléfono
        phoneNumber.addValueChangeListener(event -> validatePhoneFormat(event.getValue()));
        emergencyContactPhone.addValueChangeListener(event -> validateEmergencyPhoneFormat(event.getValue()));

        // Validación para campos numéricos
        salary.addValueChangeListener(event -> validateSalaryValue(event.getValue()));
    }

    private void validateEmailFormat(String emailValue) {
        if (emailValue != null && !emailValue.trim().isEmpty()) {
            if (!isValidEmail(emailValue)) {
                email.setInvalid(true);
                email.setErrorMessage("Formato de email inválido");
            } else {
                email.setInvalid(false);
                email.setErrorMessage(null);
            }
        }
    }

    private void validateUsernameLength(String usernameValue) {
        if (usernameValue != null) {
            if (usernameValue.length() < 3) {
                username.setInvalid(true);
                username.setErrorMessage("El usuario debe tener al menos 3 caracteres");
            } else {
                username.setInvalid(false);
                username.setErrorMessage(null);
            }
        }
    }

    private void validatePhoneFormat(String phoneValue) {
        if (phoneValue != null && !phoneValue.trim().isEmpty()) {
            if (!phoneValue.matches(DOMINICAN_PHONE_PATTERN)) {
                phoneNumber.setInvalid(true);
                phoneNumber.setErrorMessage("Formato de teléfono inválido (809, 849 o 829 + 7 dígitos)");
            } else {
                phoneNumber.setInvalid(false);
                phoneNumber.setErrorMessage(null);
            }
        }
    }

    private void validateEmergencyPhoneFormat(String phoneValue) {
        if (phoneValue != null && !phoneValue.trim().isEmpty()) {
            if (!phoneValue.matches(DOMINICAN_PHONE_PATTERN)) {
                emergencyContactPhone.setInvalid(true);
                emergencyContactPhone.setErrorMessage("Formato de teléfono inválido (809, 849 o 829 + 7 dígitos)");
            } else {
                emergencyContactPhone.setInvalid(false);
                emergencyContactPhone.setErrorMessage(null);
            }
        }
    }

    private void validateSalaryValue(Double salaryValue) {
        if (salaryValue != null && salaryValue < 0) {
            salary.setInvalid(true);
            salary.setErrorMessage("El salario no puede ser negativo");
        } else {
            salary.setInvalid(false);
            salary.setErrorMessage(null);
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches(EMAIL_PATTERN);
    }

    private void clearAllValidationErrors() {
        Stream.of(firstName, lastName, username, email, phoneNumber, province, municipality, sector, streetAddress, workSchedule)
                .forEach(field -> {
                    if (field instanceof HasValidation hasValidation) {
                        hasValidation.setInvalid(false);
                        hasValidation.setErrorMessage(null);
                    }
                });

        salary.setInvalid(false);
        salary.setErrorMessage(null);
        emergencyContactPhone.setInvalid(false);
        emergencyContactPhone.setErrorMessage(null);
    }

    private void setupRequiredFieldValidation(HasValue<?, String> field) {
        if (field instanceof HasValidation hasValidation) {
            hasValidation.setInvalid(true);
            field.addValueChangeListener(event -> {
                String value = event.getValue();
                if (value == null || value.trim().isEmpty()) {
                    hasValidation.setInvalid(true);
                    hasValidation.setErrorMessage("Este campo es requerido");
                } else {
                    hasValidation.setInvalid(false);
                    hasValidation.setErrorMessage(null);
                }
            });
        }
    }

    private EmployeeUpdateRequestDto createUpdateDtoFromEmployee(Employee employee) {
        return new EmployeeUpdateRequestDto(
                employee.getUsername(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getPhoneNumber(),
                employee.getBirthDate(),
                employee.getGender(),
                employee.getNationality(),
                employee.getProvince(),
                employee.getMunicipality(),
                employee.getSector(),
                employee.getStreetAddress(),
                employee.getEmployeeRole(),
                employee.getSalary(),
                employee.getHireDate(),
                employee.getWorkSchedule(),
                employee.getEmergencyContactName(),
                employee.getEmergencyContactPhone()
        );
    }

    public void addEmployeeSavedListener(Consumer<EmployeeCreateRequestDto> listener) {
        employeeSavedListeners.add(listener);
    }

    public void addEmployeeCancelledListener(Runnable listener) {
        employeeCancelledListeners.add(listener);
    }

    private void fireEmployeeSavedEvent(EmployeeCreateRequestDto dto) {
        employeeSavedListeners.forEach(listener -> listener.accept(dto));
    }

    private void fireEmployeeCancelledEvent() {
        employeeCancelledListeners.forEach(Runnable::run);
    }

    @Getter
    @Setter
    public static class ValidationBean {
        private String username;
        private String password;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private LocalDate birthDate;
        private Gender gender;
        private String nationality;
        private String province;
        private String municipality;
        private String sector;
        private String streetAddress;
        private EmployeeRole employeeRole;
        private Double salary;
        private LocalDate hireDate;
        private String workSchedule;
        private String emergencyContactName;
        private String emergencyContactPhone;
    }
}