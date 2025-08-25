package com.wornux.views.grooming;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
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
import com.wornux.data.entity.Invoice;
import com.wornux.data.enums.EmployeeRole;
import com.wornux.data.enums.InvoiceStatus;
import com.wornux.data.enums.SystemRole;
import com.wornux.security.UserUtils;
import com.wornux.services.implementations.InvoiceService;
import com.wornux.services.interfaces.*;
import com.wornux.utils.GridUtils;
import com.wornux.utils.NotificationUtils;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.criteria.Order;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@Route(value = "grooming", layout = com.wornux.views.MainLayout.class)
@PageTitle("Grooming")
@RolesAllowed({
  "ROLE_SYSTEM_ADMIN",
  "ROLE_MANAGER",
  "ROLE_EMP_GROOMER",
  "ROLE_EMP_KENNEL_ASSISTANT"
})
public class GroomingSessionsView extends Div {

  private final Grid<GroomingSession> grid = GridUtils.createBasicGrid(GroomingSession.class);
  private final TextField searchField = new TextField("Buscar grooming");
  private final Button create = new Button("Nuevo Grooming");
  private final Span quantity = new Span();

  private final transient GroomingSessionService groomingSessionService;
  private final transient InvoiceService invoiceService;
  private final transient OfferingService offeringService;
  private final transient ProductService productService;
  private final transient GroomingForm groomingForm;
  private final GroomingDetailsSidebar detailsSidebar;

  public GroomingSessionsView(
      @Qualifier("groomingSessionServiceImpl") GroomingSessionService groomingSessionService,
      @Qualifier("employeeServiceImpl") EmployeeService employeeService,
      @Qualifier("petServiceImpl") PetService petService,
      @Qualifier("offeringServiceImpl") OfferingService offeringService,
      @Qualifier("productServiceImpl") ProductService productService,
      InvoiceService invoiceService) {

    this.groomingSessionService = groomingSessionService;
    this.invoiceService = invoiceService;
    this.productService = productService;
    this.offeringService = offeringService;
    this.groomingForm =
        new GroomingForm(
            groomingSessionService,
            employeeService,
            petService,
            offeringService,
            invoiceService,
            productService);

    this.detailsSidebar = new GroomingDetailsSidebar(invoiceService, groomingForm);
    add(detailsSidebar);

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
    // updateQuantity();

    create.addClickListener(event -> groomingForm.openForNew());
  }

  private void createGrid(
      GroomingSessionService service, Specification<GroomingSession> specification) {
    GridUtils.configureGrid(grid, specification, service.getRepository());

    grid.asSingleSelect()
        .addValueChangeListener(
            e -> {
              if (e.getValue() != null) {
                detailsSidebar.open(e.getValue());
                grid.deselectAll();
              }
            });

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

  /*private void refreshAll() {
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
  }*/

  private void refreshAll() {
    // 1) Carga base
    List<GroomingSession> sessions =
        groomingSessionService.findAll(Pageable.unpaged()).getContent();

    // 2) Si es groomer (y no admin/manager), deja sólo sus sesiones
    if (isGroomerOnly()) {
      Long gid = currentGroomerId();
      if (gid != null) {
        sessions =
            sessions.stream()
                .filter(s -> s.getGroomer() != null && gid.equals(s.getGroomer().getId()))
                .toList();
      } else {
        sessions = Collections.emptyList();
      }
    }

    // 3) Filtro de texto
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

    // 4) Pinta y actualiza contador
    grid.setItems(sessions);
    quantity.setText("Grooming (" + sessions.size() + ")");
  }

  /*private Component createActionsColumn(GroomingSession session) {
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
  }*/

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

    // Igual que en consultas: controlar edición por estado de factura
    edit.addClickListener(e -> evaluateForEdit(session));

    delete.addClickListener(e -> showDeleteConfirmationDialog(session));

    // Mostrar "Eliminar" sólo para Manager / System Admin
    if (!(isAdminOrManager())) {
      delete.setVisible(false);
    }

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
      int size = (int) grid.getListDataView().getItems().count();
      quantity.setText("Grooming (" + size + ")");
    } catch (Exception e) {
      quantity.setText("Grooming");
    }
  }

  public void evaluateForEdit(GroomingSession session) {
    try {
      // Usa el método que tengas disponible; aquí asumimos findByGroomingId(...)
      var opt = invoiceService.findByGroomingId(session.getId());
      if (opt.isPresent()) {
        Invoice inv = opt.get();
        if (inv.getStatus() != InvoiceStatus.PENDING) {
          NotificationUtils.error(
              "No se puede editar el grooming porque su factura asociada no está en estado PENDIENTE.");
          refreshAll();
        } else {
          groomingForm.openForEdit(session);
        }
      } else {
        NotificationUtils.error("No se encontró una factura asociada a este grooming.");
        refreshAll();
      }
    } catch (Exception e) {
      NotificationUtils.error("El grooming está corrupto porque no tiene factura asociada.");
    }
  }

  private boolean isAdminOrManager() {
    return UserUtils.hasSystemRole(SystemRole.SYSTEM_ADMIN)
        || UserUtils.hasEmployeeRole(EmployeeRole.CLINIC_MANAGER);
  }

  private boolean isGroomerOnly() {
    // Si también quieres que el kennel assistant vea “todo”, déjalo fuera del filtro:
    // Sólo filtra cuando sea exclusivamente groomer y NO admin/manager
    return UserUtils.hasEmployeeRole(EmployeeRole.GROOMER) && !isAdminOrManager();
  }

  /*private Long currentGroomerId() {
    return UserUtils.getCurrentEmployee().map(Employee::getId).orElse(null);
  }*/

  private Long currentGroomerId() {
    return UserUtils.getCurrentEmployee().map(e -> e.getId()).orElse(null);
  }
}
