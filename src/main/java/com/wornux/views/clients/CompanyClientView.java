package com.wornux.views.clients;

import static com.wornux.utils.PredicateUtils.predicateForTextField;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
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
import com.wornux.data.entity.Client;
import com.wornux.services.interfaces.ClientService;
import com.wornux.services.interfaces.UserService;
import com.wornux.utils.GridUtils;
import com.wornux.utils.NotificationUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specification;
import jakarta.annotation.security.RolesAllowed;

@Slf4j
@RolesAllowed({"ROLE_SYSTEM_ADMIN", "ROLE_MANAGER", "ROLE_USER"})
@Route(value = "business-clients")
@PageTitle("Clientes Empresariales")
public class CompanyClientView extends Div {

    private final Grid<Client> grid = GridUtils.createBasicGrid(Client.class);

    private final TextField searchField = new TextField("Buscar empresas");
    private final Span quantity = new Span();

    private final Button create = new Button();
    private final transient ClientService clientService;
    private final transient UserService userService;
    private final CompanyClientForm companyClientForm;

    public CompanyClientView(@Qualifier("clientServiceImpl") ClientService clientService, @Qualifier("userServiceImpl") UserService userService) {
        this.userService = userService;
        this.clientService = clientService;
        this.companyClientForm = new CompanyClientForm(clientService, userService);

        setId("company-clients-view");

        companyClientForm.setOnSaveCallback(this::refreshAll);

        // Configure form event listeners
        companyClientForm.addClientSavedListener(
                event -> {
                    refreshAll();
                    companyClientForm.close();
                });

        companyClientForm.addClientCancelledListener(companyClientForm::close);

        createGrid(clientService, createFilterSpecification());

        final Div gridLayout = new Div(grid);
        gridLayout.addClassNames(
                LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Padding.SMALL, LumoUtility.Height.FULL);

        add(createTitle(), createFilter(), gridLayout);
        addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);
        setSizeFull();

        create.addClickListener(
                event -> {
                    companyClientForm.open();
                });
    }

    private void createGrid(ClientService service, Specification<Client> specification) {
        GridUtils.configureGrid(grid, specification, service.getRepository());

        GridUtils.addColumn(grid, Client::getRnc, "RNC", "rnc");

        GridUtils.addColumn(grid, Client::getCompanyName, "Nombre de la Empresa", "companyName");

        GridUtils.addColumn(grid, Client::getEmail, "Correo Electrónico", "email");

        GridUtils.addColumn(grid, Client::getPhoneNumber, "Teléfono", "phoneNumber");

        grid.addComponentColumn(this::renderStatus).setHeader("Estado").setTextAlign(ColumnTextAlign.CENTER);

        grid.addComponentColumn(this::createActionsColumn).setHeader("Acciones").setAutoWidth(true);

        grid.asSingleSelect()
                .addValueChangeListener(
                        event -> {
                            // TODO: Implement client editing
                        });
    }

    public Specification<Client> createFilterSpecification() {
        return (root, query, builder) -> {
            Order order = builder.desc(root.get("createdAt"));
            if (query != null) {
                query.orderBy(order);
            }

            // Filter only company clients (those with RNC)
            Predicate companyClientPredicate = builder.isNotNull(root.get("rnc"));

            Predicate searchPredicate = createSearchPredicate(root, builder);

            return builder.and(companyClientPredicate, searchPredicate);
        };
    }

    private Predicate createSearchPredicate(Root<Client> root, CriteriaBuilder builder) {
        return predicateForTextField(
                root, builder, new String[]{"companyName", "rnc", "email"}, searchField.getValue());
    }

    private void refreshAll() {
        grid.getDataProvider().refreshAll();
        long count =
                clientService.getAllActiveClients().stream()
                        .filter(client -> client.getRnc() != null)
                        .count();
        quantity.setText("Clientes Empresariales (" + count + ")");
    }

    private Component createFilter() {
        searchField.focus();
        searchField.setClearButtonVisible(true);
        searchField.setPlaceholder("Buscar por nombre de empresa, RNC, email...");
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
                LumoUtility.TextColor.PRIMARY_CONTRAST,
                LumoUtility.Background.PRIMARY,
                LumoUtility.Margin.Bottom.XSMALL);

        HorizontalLayout toolbar = new HorizontalLayout(searchField, quantity);
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        toolbar.setAlignItems(FlexComponent.Alignment.END);
        toolbar.addClassNames(
                LumoUtility.Margin.Horizontal.MEDIUM,
                LumoUtility.Margin.Top.SMALL,
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Gap.MEDIUM);

        refreshAll();

        return toolbar;
    }

    private Div createTitle() {
        final Breadcrumb breadcrumb = new Breadcrumb();

        breadcrumb.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        breadcrumb.add(
                new BreadcrumbItem("Clientes", CompanyClientView.class),
                new BreadcrumbItem("Clientes Empresariales", CompanyClientView.class));

        Icon icon = InfoIcon.INFO_CIRCLE.create("Gestionar clientes empresariales mediante RNC.");

        Div headerLayout = new Div(breadcrumb, icon);
        headerLayout.addClassNames(
                LumoUtility.Display.FLEX, LumoUtility.FlexDirection.ROW, LumoUtility.Margin.Top.SMALL);

        create.setText("Nueva Empresa");
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

    private Component createActionsColumn(Client client) {
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

        edit.addClickListener(e -> companyClientForm.openForEdit(client));
        delete.addClickListener(e -> showDeleteConfirmationDialog(client));

        HorizontalLayout actions = new HorizontalLayout(edit, delete);
        actions.setSpacing(true);
        actions.setPadding(false);
        actions.setMargin(false);
        actions.setWidth(null);
        return actions;
    }

    private void showDeleteConfirmationDialog(Client client) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirmar eliminación");
        confirmDialog.setModal(true);
        confirmDialog.setWidth("400px");

        Span message = new Span("¿Está seguro de que desea eliminar la empresa \"" +
                client.getCompanyName() + "\"? Esta acción no se puede deshacer.");
        message.getStyle().set("margin-bottom", "20px");

        Button confirmButton = new Button("Eliminar");
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirmButton.addClickListener(e -> {
            try {
                deleteClient(client);
                confirmDialog.close();
            } catch (Exception ex) {
                NotificationUtils.error("Error al eliminar la empresa: " + ex.getMessage());
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

    private void deleteClient(Client client) {
        try {
            clientService.deactivateClient(client.getId());
            NotificationUtils.success("Empresa eliminada exitosamente");
            refreshAll();
        } catch (Exception e) {
            log.error("Error deleting client", e);
            NotificationUtils.error("Error al eliminar empresa: " + e.getMessage());
        }
    }

    private Component renderStatus(Client client) {
        boolean isActive = client.isActive();
        Span badge = new Span(isActive ? "Activo" : "Inactivo");
        badge.getElement().getThemeList().add("badge pill");
        badge.getElement().getThemeList().add(isActive ? "success" : "error");
        return badge;
    }
}
