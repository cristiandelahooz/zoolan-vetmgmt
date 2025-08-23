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
import com.wornux.data.entity.Client;
import com.wornux.data.entity.GroomingSession;
import com.wornux.services.implementations.InvoiceService;
import com.wornux.services.interfaces.*;
import com.wornux.utils.GridUtils;
import com.wornux.utils.NotificationUtils;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.criteria.Order;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@Route(value = "grooming", layout = com.wornux.views.MainLayout.class)
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

    grid.asSingleSelect().addValueChangeListener(e -> {
  if (e.getValue() != null) openGroomingDetailsDialog(e.getValue());
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

  private void openGroomingDetailsDialog(GroomingSession s) {
    Dialog dialog = new Dialog();
    dialog.setHeaderTitle("Detalles de Grooming");
    dialog.setModal(true);
    dialog.setDraggable(true);
    dialog.setResizable(true);
    dialog.setWidth("640px");

    // ====== Contenedor principal con look & feel ======
    VerticalLayout layout = new VerticalLayout();
    layout.setWidthFull();
    layout.getStyle()
            .set("padding", "1.5rem")
            .set("border-radius", "10px")
            .set("background-color", "var(--lumo-base-color)")
            .set("box-shadow", "var(--lumo-box-shadow-m)");
    layout.setSpacing(true);
    layout.setPadding(false);

    // ====== Badges (estado sesión + estado factura) ======
    boolean isActive = s.getActive() != null && s.getActive();
    Span status = new Span(isActive ? "ACTIVO" : "INACTIVO");
    status.getElement().getThemeList().add("badge pill");
    status.getElement().getThemeList().add(isActive ? "success" : "error");

    Span invoiceBadge = new Span("SIN FACTURA");
    invoiceBadge.getElement().getThemeList().add("badge pill");
    invoiceBadge.getElement().getThemeList().add("contrast");
    try {
      // Usa tu método existente
      var invOpt = invoiceService.findByGroomingId(s.getId());
      if (invOpt.isPresent()) {
        var inv = invOpt.get();
        invoiceBadge.setText("FACTURA: " + inv.getStatus().name());
        invoiceBadge.getElement().getThemeList().remove("contrast");
        switch (inv.getStatus()) {
          case PAID -> invoiceBadge.getElement().getThemeList().add("success");
          case PENDING -> invoiceBadge.getElement().getThemeList().add("primary");
          default -> invoiceBadge.getElement().getThemeList().add("error");
        }
      }
    } catch (Exception ignored) {}

    HorizontalLayout header = new HorizontalLayout(status, invoiceBadge);
    header.setWidthFull();
    header.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);

    // ====== Dueño (cliente) ======
    Client owner = null;
    if (s.getPet() != null) {
      List<Client> owners = (s.getPet().getOwners() != null)
              ? s.getPet().getOwners()
              : Collections.emptyList();
      owner = owners.stream().findFirst().orElse(null);
    }

    Span clientName = new Span(owner != null
            ? owner.getFirstName() + " " + owner.getLastName()
            : "Sin dueño asignado");
    clientName.getElement().getStyle().set("font-weight", "bold").set("font-size", "1.2em");

    Icon phoneIcon = VaadinIcon.PHONE.create();
    phoneIcon.setColor("var(--lumo-secondary-text-color)");
    Span phoneText = new Span(owner != null && owner.getPhoneNumber() != null ? owner.getPhoneNumber() : "N/A");

    Icon mailIcon = VaadinIcon.ENVELOPE.create();
    mailIcon.setColor("var(--lumo-secondary-text-color)");
    Span emailText = new Span(owner != null && owner.getEmail() != null ? owner.getEmail() : "N/A");

    HorizontalLayout contactInfo = new HorizontalLayout(phoneIcon, phoneText, new Span("•"), mailIcon, emailText);
    contactInfo.setAlignItems(FlexComponent.Alignment.CENTER);
    contactInfo.setSpacing(true);

    // ====== Mascota ======
    Span petInfo = new Span(
            (s.getPet() != null ? s.getPet().getName() : "N/A")
                    + " • " + (s.getPet() != null && s.getPet().getType() != null ? s.getPet().getType().name() : "N/A")
                    + " • " + (s.getPet() != null && s.getPet().getBreed() != null ? s.getPet().getBreed() : "N/A")
                    + " • " + (s.getPet() != null && s.getPet().getGender() != null ? s.getPet().getGender().name() : "N/A")
    );
    petInfo.getElement().getStyle()
            .set("font-weight", "600")
            .set("color", "var(--lumo-primary-text-color)")
            .set("font-size", "1.05em");

    // ====== Detalles de la sesión ======
    // Groomer
    Icon groomerIcon = VaadinIcon.USER.create();
    groomerIcon.setColor("var(--lumo-secondary-text-color)");
    String groomerName = (s.getGroomer() != null)
            ? (s.getGroomer().getFirstName() + " " + s.getGroomer().getLastName())
            : "N/A";
    HorizontalLayout groomerRow = new HorizontalLayout(groomerIcon, new Span("Groomer: " + groomerName));

    // Fecha / Hora
    Icon dateIcon = VaadinIcon.CALENDAR.create();
    dateIcon.setColor("var(--lumo-secondary-text-color)");
    String dateStr = s.getGroomingDate() != null
            ? s.getGroomingDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a"))
            : "N/A";
    HorizontalLayout dateRow = new HorizontalLayout(dateIcon, new Span("Fecha de grooming: " + dateStr));

    // Notas
    Icon notesIcon = VaadinIcon.NOTEBOOK.create();
    notesIcon.setColor("var(--lumo-secondary-text-color)");
    HorizontalLayout notesRow = new HorizontalLayout(
            notesIcon, new Span("Notas: " + (s.getNotes() != null && !s.getNotes().isBlank() ? s.getNotes() : "N/A"))
    );

    for (HorizontalLayout row : new HorizontalLayout[]{groomerRow, dateRow, notesRow}) {
      row.setAlignItems(FlexComponent.Alignment.CENTER);
      row.setSpacing(true);
    }

    // ====== Acciones ======
    Button editBtn = new Button("Editar Grooming", e -> {
      groomingForm.openForEdit(s);
      dialog.close();
    });
    editBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    editBtn.setMinWidth("200px");

    Button closeBtn = new Button("Cerrar", e -> dialog.close());
    closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    closeBtn.setMinWidth("200px");

    HorizontalLayout actions = new HorizontalLayout(editBtn, closeBtn);
    actions.setJustifyContentMode(HorizontalLayout.JustifyContentMode.CENTER);
    actions.getStyle().set("margin-top", "1rem");
    actions.setWidthFull();

    layout.add(
            header,
            clientName,
            contactInfo,
            petInfo,
            groomerRow,
            dateRow,
            notesRow,
            actions
    );

    dialog.add(layout);
    dialog.addDialogCloseActionListener(e -> grid.deselectAll());
    dialog.open();
  }

}
