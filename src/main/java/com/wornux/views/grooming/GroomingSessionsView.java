package com.wornux.views.grooming;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.components.Breadcrumb;
import com.wornux.components.BreadcrumbItem;
import com.wornux.components.InfoIcon;
import com.wornux.data.entity.GroomingSession;
import com.wornux.services.implementations.InvoiceService;
import com.wornux.services.interfaces.*;
import com.wornux.utils.GridUtils;
import com.wornux.utils.NotificationUtils;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.criteria.Order;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@Route(value = "grooming")
@PageTitle("Grooming")
@RolesAllowed({"ROLE_SYSTEM_ADMIN", "ROLE_MANAGER", "ROLE_EMP_GROOMER", "ROLE_EMP_KENNEL_ASSISTANT"})
public class GroomingSessionsView extends Div {

  private final Grid<GroomingSession> grid = GridUtils.createBasicGrid(GroomingSession.class);
  private final TextField searchField = new TextField("Buscar grooming");
  private final Button create = new Button("Nuevo Grooming");
  private final Span quantity = new Span();

  private final transient GroomingSessionService groomingSessionService;
  private final transient InvoiceService invoiceService;
  private final transient ServiceService serviceService;
  private final transient ProductService productService;
  private final transient GroomingForm groomingForm;

  public GroomingSessionsView(
      @Qualifier("groomingSessionServiceImpl") GroomingSessionService groomingSessionService,
      @Qualifier("employeeServiceImpl") EmployeeService employeeService,
      @Qualifier("petServiceImpl") PetService petService,
      @Qualifier("serviceServiceImpl") ServiceService serviceService,
      @Qualifier("productServiceImpl") ProductService productService,
      InvoiceService invoiceService) {

    this.groomingSessionService = groomingSessionService;
    this.invoiceService = invoiceService;
    this.productService = productService;
    this.serviceService = serviceService;

    this.groomingForm =
        new GroomingForm(
            groomingSessionService,
            employeeService,
            petService,
            serviceService,
            invoiceService,
            productService);

    setId("grooming-view");

    groomingForm.setOnSaveCallback(
        saved -> {
          refreshAll();
          groomingForm.close();
        });

    createGrid(groomingSessionService, createFilterSpecification());

    final Div gridLayout = new Div(grid);
    gridLayout.addClassNames(
        LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Padding.SMALL, LumoUtility.Height.FULL);
    grid.addThemeVariants(
        GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);

    add(createTitle(), createFilter(), gridLayout);
    addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);
    setSizeFull();
    updateQuantity();

    create.addClickListener(event -> groomingForm.openForNew());
  }

  private void createGrid(
      GroomingSessionService service, Specification<GroomingSession> specification) {
    GridUtils.configureGrid(grid, specification, service.getRepository());

    GridUtils.addColumn(
        grid, s -> s.getPet() != null ? s.getPet().getName() : "", "Mascota", "pet");
    GridUtils.addColumn(
        grid,
        s -> s.getGroomer() != null ? s.getGroomer().getFirstName() : "",
        "Groomer",
        "groomer");
    GridUtils.addColumn(
        grid, GroomingSession::getGroomingDate, "Fecha de Grooming", "groomingDate");
    GridUtils.addColumn(grid, GroomingSession::getNotes, "Notas", "notes");
    GridUtils.addComponentColumn(grid, this::renderStatus, "Estado", "active");

    grid.addComponentColumn(this::createActionsColumn).setHeader("Acciones").setAutoWidth(true);
  }

  public Specification<GroomingSession> createFilterSpecification() {
    return (root, query, builder) -> {
      Order order = builder.desc(root.get("groomingDate"));
      if (query != null) {
        query.orderBy(order);
      }

      String filter = "%" + searchField.getValue().toLowerCase() + "%";
      return builder.or(
          builder.like(builder.lower(root.get("notes")), filter),
          builder.like(builder.lower(root.get("pet").get("name")), filter),
          builder.like(builder.lower(root.get("groomer").get("firstName")), filter),
          builder.like(builder.lower(root.get("groomer").get("lastName")), filter));
    };
  }

  private Div createTitle() {
    final Breadcrumb breadcrumb = new Breadcrumb();
    breadcrumb.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
    breadcrumb.add(
        new BreadcrumbItem("Grooming", GroomingSessionsView.class),
        new BreadcrumbItem("Lista de Grooming", GroomingSessionsView.class));

    Icon icon = InfoIcon.INFO_CIRCLE.create("Gestionar sesiones de grooming estético.");

    Div headerLayout = new Div(breadcrumb, icon);
    headerLayout.addClassNames(
        LumoUtility.Display.FLEX, LumoUtility.FlexDirection.ROW, LumoUtility.Margin.Top.SMALL);

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

  private Component createFilter() {
    searchField.setClearButtonVisible(true);
    searchField.setPlaceholder("Buscar por notas, mascota o groomer...");
    searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
    searchField.setValueChangeMode(ValueChangeMode.EAGER);
    searchField.setWidthFull();
    searchField.addValueChangeListener(e -> refreshAll());
    searchField.setWidth("50%");

    quantity.addClassNames(
        LumoUtility.BorderRadius.SMALL,
        LumoUtility.Height.XSMALL,
        LumoUtility.FontWeight.MEDIUM,
        LumoUtility.TextAlignment.CENTER,
        LumoUtility.JustifyContent.CENTER,
        LumoUtility.AlignItems.CENTER,
        LumoUtility.Padding.XSMALL,
        LumoUtility.Padding.Horizontal.SMALL,
        LumoUtility.Margin.Horizontal.SMALL,
        LumoUtility.Margin.Bottom.XSMALL,
        LumoUtility.TextColor.PRIMARY_CONTRAST,
        LumoUtility.Background.PRIMARY);
    quantity.setWidth("15%");

    HorizontalLayout toolbar = new HorizontalLayout(searchField, quantity);
    toolbar.setWidthFull();
    toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    toolbar.setAlignItems(FlexComponent.Alignment.END);
    toolbar.addClassNames(
        LumoUtility.Margin.Horizontal.MEDIUM,
        LumoUtility.Margin.Top.SMALL,
        LumoUtility.Padding.MEDIUM,
        LumoUtility.Gap.MEDIUM,
        LumoUtility.Width.FULL);

    return toolbar;
  }

  private void refreshAll() {
    List<GroomingSession> sessions =
        groomingSessionService.findAll(Pageable.unpaged()).getContent();
    String filter = searchField.getValue().trim().toLowerCase();

    if (!filter.isEmpty()) {
      sessions =
          sessions.stream()
              .filter(
                  s ->
                      (s.getNotes() != null && s.getNotes().toLowerCase().contains(filter))
                          || (s.getPet() != null
                              && s.getPet().getName() != null
                              && s.getPet().getName().toLowerCase().contains(filter))
                          || (s.getGroomer() != null
                              && ((s.getGroomer().getFirstName() != null
                                      && s.getGroomer()
                                          .getFirstName()
                                          .toLowerCase()
                                          .contains(filter))
                                  || (s.getGroomer().getLastName() != null
                                      && s.getGroomer()
                                          .getLastName()
                                          .toLowerCase()
                                          .contains(filter)))))
              .toList();
    }
    grid.setItems(sessions);
    updateQuantity();
  }

  private Component createActionsColumn(GroomingSession session) {
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

    edit.addClickListener(e -> groomingForm.openForEdit(session));
    delete.addClickListener(e -> showDeleteConfirmationDialog(session));

    HorizontalLayout actions = new HorizontalLayout(edit, delete);
    actions.setSpacing(true);
    actions.setPadding(false);
    actions.setMargin(false);
    actions.setWidth(null);
    return actions;
  }

  private void showDeleteConfirmationDialog(GroomingSession session) {
    Dialog confirmDialog = new Dialog();
    confirmDialog.setHeaderTitle("Confirmar eliminación");
    confirmDialog.setModal(true);
    confirmDialog.setWidth("400px");

    Span message =
        new Span(
            "¿Está seguro de que desea eliminar el grooming de la mascota \""
                + (session.getPet() != null ? session.getPet().getName() : "")
                + "\"? Esta acción no se puede deshacer.");
    message.getStyle().set("margin-bottom", "20px");

    Button confirmButton = new Button("Eliminar");
    confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
    confirmButton.addClickListener(
        e -> {
          try {
            groomingSessionService.delete(session.getId());
            NotificationUtils.success("Grooming eliminado exitosamente");
            refreshAll();
            confirmDialog.close();
          } catch (Exception ex) {
            NotificationUtils.error("Error al eliminar el grooming: " + ex.getMessage());
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

  private Component renderStatus(GroomingSession session) {
    boolean isActive = session.getActive() != null && session.getActive();
    Span badge = new Span(isActive ? "Activo" : "Inactivo");
    badge.getElement().getThemeList().add("badge pill");
    badge.getElement().getThemeList().add(isActive ? "success" : "error");
    return badge;
  }

  private void updateQuantity() {
    try {
      long count = groomingSessionService.getRepository().count();
      quantity.setText("Grooming (" + count + ")");
    } catch (Exception e) {
      quantity.setText("Grooming");
    }
  }
}
