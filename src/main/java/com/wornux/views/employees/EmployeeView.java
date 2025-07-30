package com.wornux.views.employees;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.components.*;
import com.wornux.data.entity.Employee;
import com.wornux.data.enums.EmployeeRole;
import com.wornux.data.enums.Gender;
import com.wornux.services.interfaces.EmployeeService;
import com.wornux.utils.GridUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.Set;

import static com.wornux.utils.PredicateUtils.createPredicateForSelectedItems;
import static com.wornux.utils.PredicateUtils.predicateForTextField;

@Slf4j
@Route(value = "employees")
@PageTitle("Empleados")
public class EmployeeView extends Div {

    private final Grid<Employee> grid = GridUtils.createBasicGrid(Employee.class);

    private final TextField searchField = new TextField("Buscar empleados");
    private final MultiSelectComboBox<EmployeeRole> role = new MultiSelectComboBox<>("Rol");
    private final MultiSelectComboBox<Gender> gender = new MultiSelectComboBox<>("Género");
    private final Span quantity = new Span();

    private final Button create = new Button();
    private final EmployeeService employeeService;
    private final EmployeeForm employeeForm;

    public EmployeeView(@Qualifier("employeeServiceImpl") EmployeeService employeeService) {
        this.employeeService = employeeService;
        this.employeeForm = new EmployeeForm(employeeService);

        setId("employees-view");

        // Configure form event listeners
        employeeForm.addEmployeeSavedListener(event -> {
            refreshAll();
        });

        employeeForm.addEmployeeCancelledListener(() -> {
            // Form handles closing automatically
        });

        createGrid(employeeService, createFilterSpecification());

        final Div gridLayout = new Div(grid);
        gridLayout.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Padding.SMALL,
                LumoUtility.Height.FULL);

        add(createTitle(), createFilter(), gridLayout);
        addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);
        setSizeFull();

        create.addClickListener(event -> {
            employeeForm.openForNew();
        });
    }

    private void createGrid(EmployeeService service, Specification<Employee> specification) {
        GridUtils.configureGrid(grid, specification, service.getRepository());

        GridUtils.addColumn(grid, Employee::getUsername, "Usuario", "username");

        GridUtils.addColumn(grid, employee -> employee.getFirstName() + " " + employee.getLastName(), "Nombre Completo",
                "firstName", "lastName");

        GridUtils.addColumn(grid, Employee::getEmail, "Correo Electrónico", "email");

        GridUtils.addColumn(grid, Employee::getPhoneNumber, "Teléfono", "phoneNumber");

        GridUtils.addColumn(grid, employee -> employee.getGender() != null ? employee.getGender().name() : "", "Género",
                "gender");

        GridUtils.addComponentColumn(grid, this::renderRole, "Rol", "employeeRole");

        GridUtils.addColumn(grid,
                employee -> "$" + String.format("%.2f", employee.getSalary() != null ? employee.getSalary() : 0.0),
                "Salario", "salary");

        GridUtils.addColumn(grid, employee -> employee.isActive() ? "Activo" : "Inactivo", "Estado", "active")
                .setTextAlign(ColumnTextAlign.CENTER);

        grid.asSingleSelect().addValueChangeListener(event -> {
            // TODO: Implement employee editing
        });
    }

    public Specification<Employee> createFilterSpecification() {
        return (root, query, builder) -> {
            Order order = builder.desc(root.get("createdAt"));
            if (query != null) {
                query.orderBy(order);
            }

            Predicate searchPredicate = createSearchPredicate(root, builder);
            Predicate rolePredicate = createRolePredicate(root, builder);
            Predicate genderPredicate = createGenderPredicate(root, builder);

            return builder.and(searchPredicate, rolePredicate, genderPredicate);
        };
    }

    private Predicate createSearchPredicate(Root<Employee> root, CriteriaBuilder builder) {
        return predicateForTextField(root, builder,
                new String[] { "username", "firstName", "lastName", "email", "phoneNumber" }, searchField.getValue());
    }

    private Predicate createRolePredicate(Root<Employee> root, CriteriaBuilder builder) {
        return createPredicateForSelectedItems(Optional.ofNullable(role.getSelectedItems()),
                items -> root.get("employeeRole").in(items), builder);
    }

    private Predicate createGenderPredicate(Root<Employee> root, CriteriaBuilder builder) {
        return createPredicateForSelectedItems(Optional.ofNullable(gender.getSelectedItems()),
                items -> root.get("gender").in(items), builder);
    }

    private void refreshAll() {
        grid.getDataProvider().refreshAll();
        long count = employeeService.getAllEmployees(org.springframework.data.domain.Pageable.unpaged())
                .getTotalElements();
        quantity.setText("Empleados (" + count + ")");
    }

    private Component createFilter() {
        searchField.focus();
        searchField.setClearButtonVisible(true);
        searchField.setPlaceholder("Buscar por nombre, email, usuario...");
        searchField.setPrefixComponent(LumoIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> refreshAll());

        quantity.addClassNames(LumoUtility.BorderRadius.SMALL, LumoUtility.Height.XSMALL, LumoUtility.FontWeight.MEDIUM,
                LumoUtility.JustifyContent.CENTER, LumoUtility.AlignItems.CENTER, LumoUtility.Padding.XSMALL,
                LumoUtility.Padding.Horizontal.SMALL, LumoUtility.Margin.Horizontal.SMALL,
                LumoUtility.TextColor.PRIMARY_CONTRAST, LumoUtility.Background.PRIMARY);

        role.setItems(EmployeeRole.values());
        role.setItemLabelGenerator(EmployeeRole::getDisplayName);

        gender.setItems(Gender.values());
        gender.setItemLabelGenerator(Gender::name);

        Set.of(role, gender).forEach(c -> {
            c.setWidthFull();
            c.setClearButtonVisible(true);
            c.setAutoExpand(MultiSelectComboBox.AutoExpandMode.BOTH);
            c.addValueChangeListener(e -> refreshAll());
        });

        HorizontalLayout toolbar = new HorizontalLayout(searchField, role, gender, quantity);
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        toolbar.setAlignItems(FlexComponent.Alignment.END);
        toolbar.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Margin.Top.SMALL,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.MEDIUM);

        refreshAll();

        return toolbar;
    }

    private Div createTitle() {
        final Breadcrumb breadcrumb = new Breadcrumb();

        breadcrumb.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        breadcrumb.add(new BreadcrumbItem("Empleados", EmployeeView.class),
                new BreadcrumbItem("Lista de Empleados", EmployeeView.class));

        Icon icon = InfoIcon.INFO_CIRCLE.create("Gestionar empleados de la clínica veterinaria.");

        Div headerLayout = new Div(breadcrumb, icon);
        headerLayout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.ROW,
                LumoUtility.Margin.Top.SMALL);

        create.setText("Nuevo Empleado");
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);
        create.addClassNames(LumoUtility.Width.AUTO);

        Div layout = new Div(headerLayout, create);
        layout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.FlexDirection.Breakpoint.Large.ROW, LumoUtility.JustifyContent.BETWEEN,
                LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Margin.Top.SMALL, LumoUtility.Gap.XSMALL,
                LumoUtility.AlignItems.STRETCH, LumoUtility.AlignItems.Breakpoint.Large.END);

        return layout;
    }

    private Component renderRole(Employee employee) {
        EmployeeRole roleValue = employee.getEmployeeRole();
        if (roleValue == null) {
            return new Span("-");
        }

        Span badge = new Span(roleValue.getDisplayName());
        badge.getElement().getThemeList().add("badge pill");

        switch (roleValue) {
        case CLINIC_MANAGER -> badge.getElement().getThemeList().add("success");
        case VETERINARIAN -> badge.getElement().getThemeList().add("primary");
        case RECEPTIONIST -> badge.getElement().getThemeList().add("contrast");
        case ADMINISTRATIVE -> badge.getElement().getThemeList().add("warning");
        case GROOMER -> badge.getElement().getThemeList().add("success primary");
        case KENNEL_ASSISTANT -> badge.getElement().getThemeList().add("contrast");
        case LAB_TECHNICIAN -> badge.getElement().getThemeList().add("primary contrast");
        }

        return badge;
    }
}
