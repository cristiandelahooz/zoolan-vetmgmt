package com.wornux.views.employees;

import static com.wornux.utils.PredicateUtils.createPredicateForSelectedItems;
import static com.wornux.utils.PredicateUtils.predicateForTextField;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.components.*;
import com.wornux.data.entity.Employee;
import com.wornux.data.enums.EmployeeRole;
import com.wornux.services.interfaces.EmployeeService;
import com.wornux.utils.GridUtils;
import com.wornux.utils.NotificationUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.Optional;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specification;

@Slf4j
@Route(value = "empleados")
@PageTitle("Empleados")
public class EmployeeView extends Div {

    private final Grid<Employee> grid = GridUtils.createBasicGrid(Employee.class);

    private final TextField searchField = new TextField("Buscar empleados");
    private final MultiSelectComboBox<EmployeeRole> role = new MultiSelectComboBox<>("Rol");
    private final Span quantity = new Span();

    private final Button create = new Button();
    private final EmployeeService employeeService;
    private final EmployeeForm employeeForm;

    public EmployeeView(@Qualifier("employeeServiceImpl") EmployeeService employeeService) {
        this.employeeService = employeeService;
        this.employeeForm = new EmployeeForm(employeeService);

        setId("employees-view");

        // Configure form event listeners
        employeeForm.addEmployeeSavedListener(event -> refreshAll());

        employeeForm.setOnSaveCallback(this::refreshAll);
        employeeForm.addEmployeeCancelledListener(
                () -> {
                    // Form handles closing automatically
                });

        createGrid(employeeService, createFilterSpecification());
        configureLazyDataView();

        final Div gridLayout = new Div(grid);
        gridLayout.addClassNames(
                LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Padding.SMALL, LumoUtility.Height.FULL);

        add(createTitle(), createFilter(), gridLayout);
        addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);
        setSizeFull();

        create.addClickListener(
                event -> {
                    employeeForm.openForNew();
                });
    }

    private void createGrid(EmployeeService service, Specification<Employee> specification) {
        GridUtils.configureGrid(grid, specification, service.getRepository());

        GridUtils.addColumn(grid, Employee::getUsername, "Usuario", "username");

        GridUtils.addColumn(
                grid,
                employee -> employee.getFirstName() + " " + employee.getLastName(),
                "Nombre Completo",
                "firstName",
                "lastName");

        GridUtils.addColumn(grid, Employee::getEmail, "Correo Electrónico", "email");

        GridUtils.addColumn(grid, Employee::getPhoneNumber, "Teléfono", "phoneNumber");

        GridUtils.addComponentColumn(grid, this::renderRole, "Rol", "employeeRole");

        GridUtils.addColumn(
                grid,
                employee ->
                        "$" + String.format("%.2f", employee.getSalary() != null ? employee.getSalary() : 0.0),
                "Salario",
                "salary");

        grid.addComponentColumn(this::renderStatus).setHeader("Estado").setAutoWidth(true);

        // Add actions column
        grid.addComponentColumn(this::createActionsColumn).setHeader("Acciones").setAutoWidth(true);

        grid.asSingleSelect()
                .addValueChangeListener(
                        event -> {
                            // Grid selection handling can be removed or used for other purposes
                        });
    }

    private Component renderStatus(Employee employee) {
        Span statusBadge = new Span(employee.isAvailable() ? "Activo" : "Inactivo");
        statusBadge.getElement().getThemeList().add("badge pill");

        if (employee.isAvailable()) {
            statusBadge.getElement().getThemeList().add("success");
        } else {
            statusBadge.getElement().getThemeList().add("error");
        }

        return statusBadge;
    }

    public Specification<Employee> createFilterSpecification() {
        return (root, query, builder) -> {
            Order order = builder.desc(root.get("createdAt"));
            if (query != null) {
                query.orderBy(order);
            }

            Predicate searchPredicate = createSearchPredicate(root, builder);
            Predicate rolePredicate = createRolePredicate(root, builder);

            return builder.and(searchPredicate, rolePredicate);
        };
    }

    private Predicate createSearchPredicate(Root<Employee> root, CriteriaBuilder builder) {
        return predicateForTextField(
                root,
                builder,
                new String[]{"username", "firstName", "lastName", "email", "phoneNumber"},
                searchField.getValue());
    }

    private Predicate createRolePredicate(Root<Employee> root, CriteriaBuilder builder) {
        return createPredicateForSelectedItems(
                Optional.ofNullable(role.getSelectedItems()),
                items -> root.get("employeeRole").in(items),
                builder);
    }



    private Component createFilter() {
        searchField.focus();
        searchField.setClearButtonVisible(true);
        searchField.setPlaceholder("Buscar por nombre, email, usuario...");
        searchField.setPrefixComponent(LumoIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> refreshAll());
        searchField.setWidth("50%");

        quantity.addClassNames(
                LumoUtility.BorderRadius.SMALL,
                LumoUtility.Height.XSMALL,
                LumoUtility.FontWeight.MEDIUM,
                LumoUtility.JustifyContent.CENTER,
                LumoUtility.AlignItems.CENTER,
                LumoUtility.Padding.XSMALL,
                LumoUtility.Padding.Horizontal.SMALL,
                LumoUtility.Margin.Horizontal.SMALL,
                LumoUtility.Margin.Bottom.XSMALL,
                LumoUtility.TextColor.PRIMARY_CONTRAST,
                LumoUtility.Background.PRIMARY);
        quantity.setWidth("15%");

        role.setItems(EmployeeRole.values());
        role.setItemLabelGenerator(EmployeeRole::getDisplayName);
        role.setWidth("20%");

        Set.of(role)
                .forEach(
                        c -> {
                            c.setClearButtonVisible(true);
                            c.setAutoExpand(MultiSelectComboBox.AutoExpandMode.BOTH);
                            c.addValueChangeListener(e -> refreshAll());
                        });

        HorizontalLayout toolbar = new HorizontalLayout(searchField, role, quantity);
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        toolbar.setAlignItems(FlexComponent.Alignment.END);
        toolbar.addClassNames(
                LumoUtility.Margin.Horizontal.MEDIUM,
                LumoUtility.Margin.Top.SMALL,
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Gap.MEDIUM,
                LumoUtility.Width.FULL);

        refreshAll();

        return toolbar;
    }

    private Div createTitle() {
        final Breadcrumb breadcrumb = new Breadcrumb();

        breadcrumb.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        breadcrumb.add(
                new BreadcrumbItem("Empleados", EmployeeView.class),
                new BreadcrumbItem("Lista de Empleados", EmployeeView.class));

        Icon icon = InfoIcon.INFO_CIRCLE.create("Gestionar empleados de la clínica veterinaria.");

        Div headerLayout = new Div(breadcrumb, icon);
        headerLayout.addClassNames(
                LumoUtility.Display.FLEX, LumoUtility.FlexDirection.ROW, LumoUtility.Margin.Top.SMALL);

        create.setText("Nuevo Empleado");
        create.addThemeVariants(
                ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);
        create.addClassNames(LumoUtility.Width.AUTO);

        Div layout = new Div(headerLayout, create);
        layout.addClassNames(
                LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN,
                LumoUtility.FlexDirection.Breakpoint.Large.ROW,
                LumoUtility.JustifyContent.BETWEEN,
                LumoUtility.Margin.Horizontal.MEDIUM,
                LumoUtility.Margin.Top.SMALL,
                LumoUtility.Gap.XSMALL,
                LumoUtility.AlignItems.STRETCH,
                LumoUtility.AlignItems.Breakpoint.Large.END);

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

    private Component createActionsColumn(Employee employee) {
        Button edit = new Button(new Icon(VaadinIcon.EDIT));
        edit.addThemeVariants(
                ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        edit.getElement().setProperty("title", "Editar");
        edit.getStyle().set("min-width", "32px").set("width", "32px").set("padding", "0");

        Button delete = new Button(new Icon(VaadinIcon.TRASH));
        delete.addThemeVariants(
                ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_TERTIARY_INLINE,
                ButtonVariant.LUMO_SMALL,
                ButtonVariant.LUMO_ERROR);
        delete.getElement().setProperty("title", "Eliminar");
        delete.getStyle().set("min-width", "32px").set("width", "32px").set("padding", "0");

        edit.addClickListener(e -> employeeForm.openForEdit(employee));
        delete.addClickListener(e -> showDeleteConfirmationDialog(employee));

        HorizontalLayout actions = new HorizontalLayout(edit, delete);
        actions.setSpacing(true);
        actions.setPadding(false);
        actions.setMargin(false);
        actions.setWidth(null);
        return actions;
    }

    private void showDeleteConfirmationDialog(Employee employee) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirmar eliminación");
        confirmDialog.setModal(true);
        confirmDialog.setWidth("400px");

        Span message = new Span("¿Está seguro de que desea eliminar al empleado \"" +
                employee.getFirstName() + " " + employee.getLastName() + "\"? Esta acción no se puede deshacer.");
        message.getStyle().set("margin-bottom", "20px");

        Button confirmButton = new Button("Eliminar");
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirmButton.addClickListener(e -> {
            try {
                deleteEmployee(employee);
                confirmDialog.close();
            } catch (Exception ex) {
                NotificationUtils.error("Error al eliminar el empleado: " + ex.getMessage());
            }
        });

        Button cancelButton = new Button("Cancelar");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(e -> confirmDialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, confirmButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setSpacing(true);

        VerticalLayout content = new VerticalLayout(message, buttonLayout);
        content.setPadding(false);
        content.setSpacing(true);

        confirmDialog.add(content);
        confirmDialog.open();
    }

    private void deleteEmployee(Employee employee) {
        try {
            employeeService.delete(employee.getId());
            NotificationUtils.success("Empleado eliminado exitosamente");
            refreshAll();
        } catch (Exception e) {
            log.error("Error deleting employee", e);
            NotificationUtils.error("Error al eliminar empleado: " + e.getMessage());
        }
    }

    private void refreshAll() {
        grid.getDataProvider().refreshAll();
        updateQuantity();
    }

    private void updateQuantity() {
        try {
            long count = employeeService.getRepository().count();
            quantity.setText("Empleados (" + count + ")");
        } catch (Exception e) {
            log.warn("Error getting employee count", e);
            quantity.setText("Empleados");
        }
    }

    private void configureLazyDataView() {
        grid.getLazyDataView().setItemCountCallback(query -> {
            return (int) employeeService.getRepository().count();
        });
    }
}