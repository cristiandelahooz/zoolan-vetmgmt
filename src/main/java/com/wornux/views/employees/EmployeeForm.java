package com.wornux.views.employees;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.Employee;
import com.wornux.data.enums.EmployeeRole;
import com.wornux.data.enums.Gender;
import com.wornux.dto.request.EmployeeCreateRequestDto;
import com.wornux.dto.request.EmployeeUpdateRequestDto;
import com.wornux.dto.request.WorkScheduleDayDto;
import com.wornux.exception.DuplicateEmployeeException;
import com.wornux.services.interfaces.EmployeeService;
import com.wornux.utils.NotificationUtils;
import jakarta.validation.ValidationException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.wornux.constants.ValidationConstants.*;

@Slf4j
public class EmployeeForm extends Dialog {

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

    private final TextField emergencyContactName = new TextField("Nombre Contacto de Emergencia");
    private final TextField emergencyContactPhone = new TextField("Teléfono Contacto de Emergencia");
    private final Grid<WorkScheduleDayDto> scheduleGrid = new Grid<>(WorkScheduleDayDto.class, false);
    private final List<WorkScheduleDayDto> workScheduleDays = new ArrayList<>();

    private final Button saveButton = new Button("Guardar");
    private final Button cancelButton = new Button("Cancelar");

    private final H3 headerTitle = new H3();

    private final Binder<ValidationBean> binder = new Binder<>(ValidationBean.class);
    private final Binder<EmployeeUpdateRequestDto> binderUpdate = new Binder<>(EmployeeUpdateRequestDto.class);

    private final transient EmployeeService employeeService;

    private final List<Consumer<EmployeeCreateRequestDto>> employeeSavedListeners = new ArrayList<>();
    private final List<Runnable> employeeCancelledListeners = new ArrayList<>();

    private boolean isEditMode = false;

    private transient Employee currentEmployee;
    private transient ValidationBean validationBean;
    private transient Runnable onSaveCallback;

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

        emergencyContactPhone.setPrefixComponent(VaadinIcon.PHONE.create());

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.addClassNames(LumoUtility.Padding.MEDIUM);

        formLayout.add(username, password, firstName, lastName, email, phoneNumber, birthDate, gender, nationality,
                province, municipality, sector, streetAddress, employeeRole, salary, hireDate);

        formLayout.add(emergencyContactName, emergencyContactPhone);

        Details scheduleDetails = createScheduleSection();
        formLayout.add(scheduleDetails);

        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

        formLayout.setColspan(streetAddress, 2);

        formLayout.setColspan(scheduleDetails, 2);

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
        binder.forField(username).asRequired("El usuario es requerido").withValidator(
                new StringLengthValidator("El usuario debe tener entre 3 y 50 caracteres", MIN_USERNAME_LENGTH,
                        MAX_USERNAME_LENGTH)).bind(ValidationBean::getUsername, ValidationBean::setUsername);

        binder.forField(password).asRequired("La contraseña es requerida").withValidator(
                        new StringLengthValidator("La contraseña debe tener al menos 8 caracteres", MIN_PASSWORD_LENGTH, null))
                .bind(ValidationBean::getPassword, ValidationBean::setPassword);

        binder.forField(firstName).asRequired("El nombre es requerido")
                .withValidator(new StringLengthValidator("El nombre debe tener al menos 2 caracteres", 2, null))
                .bind(ValidationBean::getFirstName, ValidationBean::setFirstName);

        binder.forField(lastName).asRequired("El apellido es requerido")
                .withValidator(new StringLengthValidator("El apellido debe tener al menos 2 caracteres", 2, null))
                .bind(ValidationBean::getLastName, ValidationBean::setLastName);

        binder.forField(email).withValidator(new EmailValidator("Proporcione un correo electrónico válido"))
                .bind(ValidationBean::getEmail, ValidationBean::setEmail);

        binder.forField(phoneNumber).withValidator(
                        new RegexpValidator("Proporcione un número de teléfono válido", DOMINICAN_PHONE_PATTERN, true))
                .bind(ValidationBean::getPhoneNumber, ValidationBean::setPhoneNumber);

        binder.forField(birthDate).withValidator(this::validateBirthDate)
                .bind(ValidationBean::getBirthDate, ValidationBean::setBirthDate);

        binder.forField(gender).bind(ValidationBean::getGender, ValidationBean::setGender);

        binder.forField(nationality).bind(ValidationBean::getNationality, ValidationBean::setNationality);

        binder.forField(province).asRequired("La provincia es requerida")
                .bind(ValidationBean::getProvince, ValidationBean::setProvince);

        binder.forField(municipality).asRequired("El municipio es requerido")
                .bind(ValidationBean::getMunicipality, ValidationBean::setMunicipality);

        binder.forField(sector).asRequired("El sector es requerido")
                .bind(ValidationBean::getSector, ValidationBean::setSector);

        binder.forField(streetAddress).asRequired("La dirección es requerida")
                .bind(ValidationBean::getStreetAddress, ValidationBean::setStreetAddress);

        binder.forField(employeeRole).asRequired("El rol de empleado es requerido")
                .bind(ValidationBean::getEmployeeRole, ValidationBean::setEmployeeRole);

        binder.forField(salary).withValidator(this::validateSalary)
                .bind(ValidationBean::getSalary, ValidationBean::setSalary);

        binder.forField(hireDate).asRequired("La fecha de contratación es requerida")
                .bind(ValidationBean::getHireDate, ValidationBean::setHireDate);

        binder.forField(emergencyContactName)
                .bind(ValidationBean::getEmergencyContactName, ValidationBean::setEmergencyContactName);

        binder.forField(emergencyContactPhone)
                .bind(ValidationBean::getEmergencyContactPhone, ValidationBean::setEmergencyContactPhone);
    }

    private void setupUpdateBinder() {
        binderUpdate.forField(username).withValidator(
                        new StringLengthValidator("El usuario debe tener entre 3 y 50 caracteres", MIN_USERNAME_LENGTH,
                                MAX_USERNAME_LENGTH))
                .bind(EmployeeUpdateRequestDto::getUsername, EmployeeUpdateRequestDto::setUsername);

        binderUpdate.forField(firstName)
                .withValidator(new StringLengthValidator("El nombre debe tener al menos 2 caracteres", 2, null))
                .bind(EmployeeUpdateRequestDto::getFirstName, EmployeeUpdateRequestDto::setFirstName);

        binderUpdate.forField(lastName)
                .withValidator(new StringLengthValidator("El apellido debe tener al menos 2 caracteres", 2, null))
                .bind(EmployeeUpdateRequestDto::getLastName, EmployeeUpdateRequestDto::setLastName);

        binderUpdate.forField(email).withValidator(new EmailValidator("Proporcione un correo electrónico válido"))
                .bind(EmployeeUpdateRequestDto::getEmail, EmployeeUpdateRequestDto::setEmail);

        binderUpdate.forField(phoneNumber).withValidator(
                        new RegexpValidator("Proporcione un número de teléfono válido", DOMINICAN_PHONE_PATTERN, true))
                .bind(EmployeeUpdateRequestDto::getPhoneNumber, EmployeeUpdateRequestDto::setPhoneNumber);

        binderUpdate.forField(birthDate).withValidator(this::validateBirthDate)
                .bind(EmployeeUpdateRequestDto::getBirthDate, EmployeeUpdateRequestDto::setBirthDate);

        binderUpdate.forField(gender).bind(EmployeeUpdateRequestDto::getGender, EmployeeUpdateRequestDto::setGender);

        binderUpdate.forField(nationality)
                .bind(EmployeeUpdateRequestDto::getNationality, EmployeeUpdateRequestDto::setNationality);

        binderUpdate.forField(province).asRequired("La provincia es requerida")
                .bind(EmployeeUpdateRequestDto::getProvince, EmployeeUpdateRequestDto::setProvince);

        binderUpdate.forField(municipality).asRequired("El municipio es requerido")
                .bind(EmployeeUpdateRequestDto::getMunicipality, EmployeeUpdateRequestDto::setMunicipality);

        binderUpdate.forField(sector).asRequired("El sector es requerido")
                .bind(EmployeeUpdateRequestDto::getSector, EmployeeUpdateRequestDto::setSector);

        binderUpdate.forField(streetAddress).asRequired("La dirección es requerida")
                .bind(EmployeeUpdateRequestDto::getStreetAddress, EmployeeUpdateRequestDto::setStreetAddress);

        binderUpdate.forField(employeeRole).asRequired("El rol de empleado es requerido")
                .bind(EmployeeUpdateRequestDto::getEmployeeRole, EmployeeUpdateRequestDto::setEmployeeRole);

        binderUpdate.forField(salary).withValidator(this::validateSalary)
                .bind(EmployeeUpdateRequestDto::getSalary, EmployeeUpdateRequestDto::setSalary);

        binderUpdate.forField(hireDate).asRequired("La fecha de contratación es requerida")
                .bind(EmployeeUpdateRequestDto::getHireDate, EmployeeUpdateRequestDto::setHireDate);

        binderUpdate.forField(emergencyContactName).bind(EmployeeUpdateRequestDto::getEmergencyContactName,
                EmployeeUpdateRequestDto::setEmergencyContactName);

        binderUpdate.forField(emergencyContactPhone).bind(EmployeeUpdateRequestDto::getEmergencyContactPhone,
                EmployeeUpdateRequestDto::setEmergencyContactPhone);
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
        if (!binder.writeBeanIfValid(validationBean)) {
            NotificationUtils.error("Por favor, corrija los errores en el formulario");
            return;
        }

        try {
            if (isNullOrEmpty(validationBean.getUsername(), "El nombre de usuario es requerido")) {
                username.focus();
                return;
            }
            if (isNullOrEmpty(validationBean.getPassword(), "La contraseña es requerida")) {
                password.focus();
                return;
            }

            log.debug("Creating employee with username: '{}', firstName: '{}', lastName: '{}'",
                    validationBean.getUsername(), validationBean.getFirstName(), validationBean.getLastName());

            EmployeeCreateRequestDto dto = buildEmployeeCreateRequestDto();

            employeeService.save(dto);
            NotificationUtils.success("Empleado creado exitosamente");
            close();
            fireEmployeeSavedEvent(dto);
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
        } catch (DuplicateEmployeeException e) {
            log.error("Duplicate employee error", e);
            NotificationUtils.error("Ya existe un empleado con ese nombre de usuario");
            username.focus();
        } catch (ValidationException e) {
            log.error("Validation error saving employee", e);
            NotificationUtils.error("Error de validación: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error saving employee", e);
            NotificationUtils.error("Error al guardar el empleado: " + e.getMessage());
        }
    }

    private boolean isNullOrEmpty(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            NotificationUtils.error(errorMessage);
            return true;
        }
        return false;
    }

    private EmployeeCreateRequestDto buildEmployeeCreateRequestDto() {
        return EmployeeCreateRequestDto.builder().username(validationBean.getUsername().trim())
                .password(validationBean.getPassword()).firstName(trimOrNull(validationBean.getFirstName()))
                .lastName(trimOrNull(validationBean.getLastName())).email(trimOrNull(validationBean.getEmail()))
                .phoneNumber(trimOrNull(validationBean.getPhoneNumber())).birthDate(validationBean.getBirthDate())
                .gender(validationBean.getGender()).nationality(trimOrNull(validationBean.getNationality()))
                .province(trimOrNull(validationBean.getProvince()))
                .municipality(trimOrNull(validationBean.getMunicipality()))
                .sector(trimOrNull(validationBean.getSector()))
                .streetAddress(trimOrNull(validationBean.getStreetAddress()))
                .employeeRole(validationBean.getEmployeeRole()).salary(validationBean.getSalary())
                .hireDate(validationBean.getHireDate()).workScheduleDays(new ArrayList<>(workScheduleDays))
                .emergencyContactName(trimOrNull(validationBean.getEmergencyContactName()))
                .emergencyContactPhone(trimOrNull(validationBean.getEmergencyContactPhone())).build();
    }

    private String trimOrNull(String value) {
        return value != null ? value.trim() : null;
    }

    public void openForNew() {
        isEditMode = false;
        clearAllValidationErrors();
        currentEmployee = null;
        validationBean = new ValidationBean();

        updateHeaderTitle("Nuevo Empleado");
        clearForm();

        initializeDefaultSchedule();
        scheduleGrid.getDataProvider().refreshAll();

        binder.setBean(validationBean);

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

        loadEmployeeSchedule(employee);

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

        emergencyContactName.setValue(
                employee.getEmergencyContactName() != null ? employee.getEmergencyContactName() : "");
        emergencyContactPhone.setValue(
                employee.getEmergencyContactPhone() != null ? employee.getEmergencyContactPhone() : "");
    }

    private void configureFieldValidation() {
        Stream.of(firstName, lastName, username, email, phoneNumber, province, municipality, sector, streetAddress)
                .forEach(this::setupRequiredFieldValidation);

        email.addValueChangeListener(event -> validateEmailFormat(event.getValue()));
        username.addValueChangeListener(event -> validateUsernameLength(event.getValue()));
        phoneNumber.addValueChangeListener(event -> validatePhoneFormat(event.getValue()));
        emergencyContactPhone.addValueChangeListener(event -> validateEmergencyPhoneFormat(event.getValue()));
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
        Stream.of(firstName, lastName, username, email, phoneNumber, province, municipality, sector, streetAddress)
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

    private HorizontalLayout createScheduleTemplateButtons() {
        Button mondayToFriday = new Button("Lunes a Viernes (8-17)");
        mondayToFriday.addClickListener(e -> applyMondayToFridayTemplate());

        Button fullTime = new Button("Tiempo Completo (8-18)");
        fullTime.addClickListener(e -> applyFullTimeTemplate());

        Button partTime = new Button("Medio Tiempo (8-12)");
        partTime.addClickListener(e -> applyPartTimeTemplate());

        Button clearAll = new Button("Limpiar Horario");
        clearAll.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearAll.addClickListener(e -> clearSchedule());

        HorizontalLayout templateLayout = new HorizontalLayout(mondayToFriday, fullTime, partTime, clearAll);
        templateLayout.setSpacing(true);
        templateLayout.addClassNames(LumoUtility.Margin.Bottom.SMALL);

        return templateLayout;
    }

    private VerticalLayout createScheduleContent() {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);

        if (workScheduleDays.isEmpty()) {
            initializeDefaultSchedule();
        }

        content.add(createScheduleTemplateButtons());

        setupScheduleGrid();
        content.add(scheduleGrid);

        return content;
    }

    private Details createScheduleSection() {
        Details scheduleDetails = new Details("Horario de Trabajo Detallado", createScheduleContent());
        scheduleDetails.setOpened(true);
        scheduleDetails.setHeightFull();
        return scheduleDetails;
    }

    private EmployeeUpdateRequestDto createUpdateDtoFromEmployee(Employee employee) {
        EmployeeUpdateRequestDto dto = new EmployeeUpdateRequestDto();

        // Campos básicos
        dto.setUsername(employee.getUsername());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setPhoneNumber(employee.getPhoneNumber());
        dto.setBirthDate(employee.getBirthDate());
        dto.setGender(employee.getGender());
        dto.setNationality(employee.getNationality());
        dto.setProvince(employee.getProvince());
        dto.setMunicipality(employee.getMunicipality());
        dto.setSector(employee.getSector());
        dto.setStreetAddress(employee.getStreetAddress());
        dto.setEmployeeRole(employee.getEmployeeRole());
        dto.setSalary(employee.getSalary());
        dto.setHireDate(employee.getHireDate());
        dto.setEmergencyContactName(employee.getEmergencyContactName());
        dto.setEmergencyContactPhone(employee.getEmergencyContactPhone());

        List<WorkScheduleDayDto> scheduleDtos = new ArrayList<>();
        if (employee.getWorkScheduleDays() != null) {
            scheduleDtos = employee.getWorkScheduleDays().stream()
                    .map(day -> WorkScheduleDayDto.builder().dayOfWeek(day.getDayOfWeek()).startTime(day.getStartTime())
                            .endTime(day.getEndTime()).isOffDay(day.isOffDay()).build()).toList();
        }
        dto.setWorkScheduleDays(scheduleDtos);

        return dto;
    }

    private void loadEmployeeSchedule(Employee employee) {
        workScheduleDays.clear();

        if (employee.getWorkScheduleDays() != null && !employee.getWorkScheduleDays().isEmpty()) {
            List<WorkScheduleDayDto> scheduleDtos = employee.getWorkScheduleDays().stream()
                    .map(day -> WorkScheduleDayDto.builder().dayOfWeek(day.getDayOfWeek()).startTime(day.getStartTime())
                            .endTime(day.getEndTime()).isOffDay(day.isOffDay()).build()).toList();
            workScheduleDays.addAll(scheduleDtos);
        } else {
            initializeDefaultSchedule();
        }

        scheduleGrid.getDataProvider().refreshAll();
    }

    private void saveUpdate() {
        if (!binderUpdate.validate().isOk()) {
            NotificationUtils.error("Por favor, corrija los errores en el formulario");
            return;
        }

        try {
            EmployeeUpdateRequestDto updateDto = binderUpdate.getBean();

            updateDto.setWorkScheduleDays(new ArrayList<>(workScheduleDays));

            employeeService.updateEmployee(currentEmployee.getId(), updateDto);
            NotificationUtils.success("Empleado actualizado exitosamente");
            close();
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
        } catch (DuplicateEmployeeException e) {
            NotificationUtils.error("Ya existe un empleado con ese nombre de usuario o correo");
        } catch (ValidationException e) {
            NotificationUtils.error("Error de validación: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error updating employee", e);
            NotificationUtils.error("Error al actualizar empleado: " + e.getMessage());
        }
    }

    private void initializeDefaultSchedule() {
        workScheduleDays.clear();
        for (DayOfWeek day : DayOfWeek.values()) {
            WorkScheduleDayDto scheduleDay = WorkScheduleDayDto.builder().dayOfWeek(day).startTime(null).endTime(null)
                    .isOffDay(true).build();
            workScheduleDays.add(scheduleDay);
        }
    }

    private void setupScheduleGrid() {
        scheduleGrid.removeAllColumns();

        scheduleGrid.addColumn(WorkScheduleDayDto::getDayOfWeek).setHeader("Día").setAutoWidth(true);

        scheduleGrid.addComponentColumn(day -> {
            Checkbox offDayCheckbox = new Checkbox();
            offDayCheckbox.setValue(day.isOffDay());
            offDayCheckbox.addValueChangeListener(e -> {
                day.setOffDay(e.getValue());
                if (Boolean.TRUE.equals(e.getValue())) {
                    day.setStartTime(null);
                    day.setEndTime(null);
                }
                scheduleGrid.getDataProvider().refreshItem(day);
            });
            return offDayCheckbox;
        }).setHeader("Día Libre").setAutoWidth(true);

        scheduleGrid.addComponentColumn(day -> {
            TimePicker startTimePicker = createStartTimeField(day);
            startTimePicker.setValue(day.getStartTime());
            startTimePicker.setEnabled(!day.isOffDay());
            return startTimePicker;
        }).setHeader("Hora Inicio").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);

        scheduleGrid.addComponentColumn(day -> {
            TimePicker endTimePicker = new TimePicker();
            endTimePicker.setValue(day.getEndTime());
            endTimePicker.setEnabled(!day.isOffDay());
            endTimePicker.addValueChangeListener(e -> day.setEndTime(e.getValue()));
            return endTimePicker;
        }).setHeader("Hora Fin").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);

        scheduleGrid.setItems(workScheduleDays);
    }

    private TimePicker createStartTimeField(WorkScheduleDayDto day) {
        TimePicker startTimePicker = new TimePicker();
        startTimePicker.setValue(day.getStartTime());
        startTimePicker.addValueChangeListener(e -> day.setStartTime(e.getValue()));
        return startTimePicker;
    }

    private void applyMondayToFridayTemplate() {
        workScheduleDays.forEach(day -> {
            if (day.getDayOfWeek().getValue() <= 5) {
                day.setStartTime(LocalTime.of(8, 0));
                day.setEndTime(LocalTime.of(17, 0));
                day.setOffDay(false);
            } else {
                day.setOffDay(true);
                day.setStartTime(null);
                day.setEndTime(null);
            }
        });
        scheduleGrid.getDataProvider().refreshAll();
    }

    private void applyFullTimeTemplate() {
        workScheduleDays.forEach(day -> {
            if (day.getDayOfWeek().getValue() <= 6) { // Monday to Saturday
                day.setStartTime(LocalTime.of(8, 0));
                day.setEndTime(LocalTime.of(18, 0));
                day.setOffDay(false);
            } else {
                day.setOffDay(true);
                day.setStartTime(null);
                day.setEndTime(null);
            }
        });
        scheduleGrid.getDataProvider().refreshAll();
    }

    private void applyPartTimeTemplate() {
        workScheduleDays.forEach(day -> {
            if (day.getDayOfWeek().getValue() <= 5) { // Monday to Friday
                day.setStartTime(LocalTime.of(8, 0));
                day.setEndTime(LocalTime.of(12, 0));
                day.setOffDay(false);
            } else {
                day.setOffDay(true);
                day.setStartTime(null);
                day.setEndTime(null);
            }
        });
        scheduleGrid.getDataProvider().refreshAll();
    }

    private void clearSchedule() {
        workScheduleDays.forEach(day -> {
            day.setOffDay(true);
            day.setStartTime(null);
            day.setEndTime(null);
        });
        scheduleGrid.getDataProvider().refreshAll();
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

        private String emergencyContactName;
        private String emergencyContactPhone;
    }
}