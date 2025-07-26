package com.wornux.views.employees;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Section;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.dto.request.EmployeeCreateRequestDto;
import com.wornux.data.enums.EmployeeRole;
import com.wornux.data.enums.Gender;
import com.wornux.services.implementations.EmployeeServiceImpl;
import lombok.extern.slf4j.Slf4j;

import static com.wornux.constants.ValidationConstants.*;

@Slf4j
@PageTitle("Register Employee")
@Route("employees/form")
public class EmployeeForm extends Div {
    private final TextField username = new TextField("Usuario");
    private final PasswordField password = new PasswordField("Contraseña");
    private final TextField firstName = new TextField("Nombre");
    private final TextField lastName = new TextField("Apellido");
    private final EmailField email = new EmailField("Correo electrónico");
    private final TextField phoneNumber = new TextField("Teléfono");
    private final DatePicker birthDate = new DatePicker("Fecha de nacimiento");
    private final ComboBox<Gender> gender = new ComboBox<>("Género");
    private final TextField nationality = new TextField("Nacionalidad");

    private final TextField province = new TextField("Provincia");
    private final TextField municipality = new TextField("Municipio");
    private final TextField sector = new TextField("Sector");
    private final TextField streetAddress = new TextField("Dirección");

    private final ComboBox<EmployeeRole> employeeRole = new ComboBox<>("Rol");
    private final TextField salary = new TextField("Salario");
    private final DatePicker hireDate = new DatePicker("Fecha de contratación");
    private final TextField workSchedule = new TextField("Horario laboral");

    private final TextField emergencyContactName = new TextField("Nombre de contacto de emergencia");
    private final TextField emergencyContactPhone = new TextField("Teléfono de contacto de emergencia");

    private transient EmployeeServiceImpl employeeService;

    public EmployeeForm(EmployeeServiceImpl employeeService) {
        this.employeeService = employeeService;
        addClassNames(LumoUtility.Display.GRID, LumoUtility.Margin.Horizontal.AUTO,
                LumoUtility.Padding.Horizontal.MEDIUM, LumoUtility.Gap.LARGE, LumoUtility.MaxWidth.SCREEN_MEDIUM,
                LumoUtility.Margin.Top.XLARGE);
        setWidthFull();
        gender.setItems(Gender.values());
        employeeRole.setItems(EmployeeRole.values());

        add(createHeaderSection(), createUserInfoSection(), createAddressSection(), createEmployeeInfoSection(),
                createFooterButtons());
    }

    private Section createHeaderSection() {
        H1 title = new H1("Registrar nuevo empleado");
        Section headerSection = new Section(title);
        headerSection.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Gap.XLARGE,
                LumoUtility.JustifyContent.BETWEEN, LumoUtility.Width.FULL);
        return headerSection;
    }

    private Div createFooterButtons() {
        Button saveButton = new Button("Guardar", event -> saveEmployee());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Cancelar", event -> Notification.show("Registro cancelado"));
        Div buttonGroup = new Div(saveButton, cancelButton);
        buttonGroup.addClassNames(LumoUtility.Gap.MEDIUM, LumoUtility.Display.FLEX, LumoUtility.JustifyContent.END);
        return buttonGroup;
    }

    private Section createUserInfoSection() {
        H4 sectionTitle = new H4("Información del usuario");
        FormLayout form = createUserInfoForm();
        Section section = new Section(sectionTitle, form);
        section.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Gap.MEDIUM,
                LumoUtility.Margin.Top.LARGE);
        return section;
    }

    private Section createAddressSection() {
        H4 sectionTitle = new H4("Dirección del empleado");
        FormLayout form = createAddressForm();
        Section section = new Section(sectionTitle, form);
        section.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Gap.MEDIUM,
                LumoUtility.Margin.Top.LARGE);
        return section;
    }

    private Section createEmployeeInfoSection() {
        H4 sectionTitle = new H4("Información del empleado");
        FormLayout form = createEmployeeInfoForm();
        Section section = new Section(sectionTitle, form);
        section.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Gap.MEDIUM,
                LumoUtility.Margin.Top.LARGE);
        return section;
    }

    private void saveEmployee() {
        if (!validateFields()) {
            return;
        }
        try {
            EmployeeCreateRequestDto dto = EmployeeCreateRequestDto.builder().username(username.getValue())
                    .password(password.getValue()).firstName(firstName.getValue()).lastName(lastName.getValue())
                    .email(email.getValue()).phoneNumber(phoneNumber.getValue()).birthDate(birthDate.getValue())
                    .gender(gender.getValue()).nationality(nationality.getValue()).province(province.getValue())
                    .municipality(municipality.getValue()).sector(sector.getValue())
                    .streetAddress(streetAddress.getValue()).employeeRole(employeeRole.getValue())
                    .salary(Double.valueOf(salary.getValue())).hireDate(hireDate.getValue())
                    .workSchedule(workSchedule.getValue()).emergencyContactName(emergencyContactName.getValue())
                    .emergencyContactPhone(emergencyContactPhone.getValue()).build();
            employeeService.save(dto);
            handleOnSuccess();
        } catch (Exception e) {
            handleOnError(e.getMessage());
        }
    }

    private FormLayout createUserInfoForm() {
        FormLayout form = new FormLayout();
        username.setRequired(true);
        username.setMinLength(MIN_USERNAME_LENGTH);
        username.setMaxLength(MAX_USERNAME_LENGTH);
        username.setErrorMessage("Nombre de usuario debe tener entre 3 y 50 caracteres");
        password.setRequired(true);
        password.setMinLength(MIN_PASSWORD_LENGTH);
        password.setErrorMessage("La contraseña debe tener al menos 8 caracteres");
        firstName.setRequired(true);
        lastName.setRequired(true);
        email.setRequired(true);
        email.setErrorMessage("Correo electrónico inválido");
        phoneNumber.setRequired(true);
        phoneNumber.setPattern(DOMINICAN_PHONE_PATTERN);
        phoneNumber.setErrorMessage("Por favor, proporciona un número de teléfono dominicano válido");
        birthDate.setRequired(true);
        gender.setRequired(true);
        form.add(username, password, firstName, lastName, email, phoneNumber, birthDate, gender, nationality);
        form.setColspan(email, 2);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("20rem", 2));
        form.addClassNames(LumoUtility.Gap.LARGE, LumoUtility.Width.FULL);
        return form;
    }

    private FormLayout createEmployeeInfoForm() {
        FormLayout form = new FormLayout();
        employeeRole.setRequired(true);
        salary.setRequired(true);
        hireDate.setRequired(true);
        workSchedule.setRequired(true);
        form.add(employeeRole, salary, hireDate, workSchedule, emergencyContactName, emergencyContactPhone);
        form.addClassNames(LumoUtility.Gap.LARGE, LumoUtility.Width.FULL);
        return form;
    }

    private FormLayout createAddressForm() {
        FormLayout form = new FormLayout();
        streetAddress.setRequired(true);
        sector.setRequired(true);
        form.add(province, municipality, sector, streetAddress);
        form.addClassNames(LumoUtility.Gap.LARGE, LumoUtility.Width.FULL);
        return form;
    }

    private boolean validateFields() {
        boolean anyBlank = isBlank(username.getValue()) || isBlank(password.getValue()) || isBlank(firstName.getValue())
                || isBlank(lastName.getValue()) || isBlank(email.getValue()) || isBlank(phoneNumber.getValue())
                || birthDate.isEmpty() || gender.isEmpty() || isBlank(province.getValue())
                || isBlank(municipality.getValue()) || isBlank(sector.getValue()) || isBlank(streetAddress.getValue())
                || employeeRole.isEmpty() || isBlank(salary.getValue()) || hireDate.isEmpty()
                || isBlank(workSchedule.getValue());
        if (anyBlank) {
            handleOnError("Por favor, complete todos los campos requeridos.");
            return false;
        }
        return true;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void handleOnSuccess() {
        Notification.show("Empleado guardado exitosamente", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        UI.getCurrent().navigate("/employees");
    }

    private void handleOnError(String errorMessage) {
        Notification.show(errorMessage, 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}