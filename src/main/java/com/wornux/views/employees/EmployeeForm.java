package com.wornux.views.employees;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
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
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@PageTitle("Register Employee")
@Route("employees/form")
public class EmployeeForm extends Div {
    @Autowired
    private EmployeeServiceImpl employeeService;

    private TextField username = new TextField("Usuario");
    private PasswordField password = new PasswordField("Contraseña");
    private TextField firstName = new TextField("Nombre");
    private TextField lastName = new TextField("Apellido");
    private EmailField email = new EmailField("Correo electrónico");
    private TextField phoneNumber = new TextField("Teléfono");
    private DatePicker birthDate = new DatePicker("Fecha de nacimiento");
    private ComboBox<Gender> gender = new ComboBox<>("Género");
    private TextField nationality = new TextField("Nacionalidad");

    private TextField province = new TextField("Provincia");
    private TextField municipality = new TextField("Municipio");
    private TextField sector = new TextField("Sector");
    private TextField streetAddress = new TextField("Dirección");

    private ComboBox<EmployeeRole> employeeRole = new ComboBox<>("Rol");
    private TextField salary = new TextField("Salario");
    private DatePicker hireDate = new DatePicker("Fecha de contratación");
    private TextField workSchedule = new TextField("Horario laboral");
    private TextField emergencyContactName = new TextField("Nombre de contacto de emergencia");
    private TextField emergencyContactPhone = new TextField("Teléfono de contacto de emergencia");
    private TextField profilePicture = new TextField("URL de foto de perfil");


    public EmployeeForm() {
        addClassNames(
                LumoUtility.Display.GRID,
                LumoUtility.Margin.Horizontal.AUTO,
                LumoUtility.Padding.Horizontal.MEDIUM,
                LumoUtility.Gap.LARGE,
                LumoUtility.MaxWidth.SCREEN_MEDIUM,
                LumoUtility.Margin.Top.XLARGE
        );
        setWidthFull();
        gender.setItems(Gender.values());
        employeeRole.setItems(EmployeeRole.values());
        add(
                createHeaderSection(),
                createUserInfoSection(),
                createAddressSection(),
                createEmployeeInfoSection(),
                createFooterButtons()
        );
    }

    private Section createHeaderSection() {
        H1 title = new H1("Registrar nuevo empleado");
        Section headerSection = new Section(title);
        headerSection.addClassNames(
                LumoUtility.Display.FLEX,
                LumoUtility.Gap.XLARGE,
                LumoUtility.JustifyContent.BETWEEN,
                LumoUtility.Width.FULL
        );
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
        FormLayout form = new FormLayout();
        form.add(username, password, firstName, lastName, email, phoneNumber, birthDate, gender, nationality, profilePicture);
        form.setColspan(email, 2);
        form.setColspan(profilePicture, 2);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("20rem", 2)
        );
        form.addClassNames(LumoUtility.Gap.LARGE, LumoUtility.Width.FULL);

        Section section = new Section(sectionTitle, form);
        section.addClassNames(
                LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Gap.MEDIUM,
                LumoUtility.Margin.Top.LARGE
        );
        return section;
    }


    private Section createAddressSection() {
        H4 sectionTitle = new H4("Dirección del empleado");
        FormLayout form = new FormLayout();
        form.add(province, municipality, sector, streetAddress);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("20rem", 2)
        );
        form.addClassNames(LumoUtility.Gap.LARGE, LumoUtility.Width.FULL);

        Section section = new Section(sectionTitle, form);
        section.addClassNames(
                LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Gap.MEDIUM,
                LumoUtility.Margin.Top.LARGE
        );
        return section;
    }

    private Section createEmployeeInfoSection() {
        H4 sectionTitle = new H4("Información del empleado");
        FormLayout form = new FormLayout();
        form.add(employeeRole, salary, hireDate, workSchedule, emergencyContactName, emergencyContactPhone);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("20rem", 2)
        );
        form.addClassNames(LumoUtility.Gap.LARGE, LumoUtility.Width.FULL);

        Section section = new Section(sectionTitle, form);
        section.addClassNames(
                LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Gap.MEDIUM,
                LumoUtility.Margin.Top.LARGE
        );
        return section;
    }

    private void saveEmployee() {
        try {
            EmployeeCreateRequestDto dto = EmployeeCreateRequestDto.builder()
                    .username(username.getValue())
                    .password(password.getValue())
                    .firstName(firstName.getValue())
                    .lastName(lastName.getValue())
                    .email(email.getValue())
                    .phoneNumber(phoneNumber.getValue())
                    .birthDate(birthDate.getValue())
                    .gender(gender.getValue())
                    .nationality(nationality.getValue())
                    .profilePicture(profilePicture.getValue())
                    .province(province.getValue())
                    .municipality(municipality.getValue())
                    .sector(sector.getValue())
                    .streetAddress(streetAddress.getValue())
                    .employeeRole(employeeRole.getValue())
                    .salary(salary.getValue() != null && !salary.getValue().isEmpty() ? Double.valueOf(salary.getValue()) : null)
                    .hireDate(hireDate.getValue())
                    .workSchedule(workSchedule.getValue())
                    .emergencyContactName(emergencyContactName.getValue())
                    .emergencyContactPhone(emergencyContactPhone.getValue())
                    .build();

            employeeService.createEmployee(dto);
            handleOnSuccess();
        } catch (Exception e) {
            Notification.show("Error al guardar el empleado: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void handleOnSuccess() {
        Notification.show("Empleado guardado exitosamente", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        UI.getCurrent().navigate("/employees");
    }
}