package com.wornux.views.consultations;

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
import com.wornux.data.entity.Consultation;
import com.wornux.data.entity.Invoice;
import com.wornux.data.enums.EmployeeRole;
import com.wornux.data.enums.InvoiceStatus;
import com.wornux.data.enums.SystemRole;
import com.wornux.security.UserUtils;
import com.wornux.services.implementations.InvoiceService;
import com.wornux.services.interfaces.*;
import com.wornux.utils.GridUtils;
import com.wornux.utils.NotificationUtils;
import com.wornux.views.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.criteria.Order;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;

@Slf4j
@Route(value = "consultations", layout = MainLayout.class)
@PageTitle("Consultas")
@RolesAllowed({"ROLE_SYSTEM_ADMIN", "ROLE_MANAGER", "ROLE_EMP_VETERINARIAN"})
public class ConsultationsView extends Div {

  private final Grid<Consultation> grid = GridUtils.createBasicGrid(Consultation.class);
  private final TextField searchField = new TextField("Buscar consultas");
  private final Button create = new Button("Nueva Consulta");
  private final Span quantity = new Span();
  private final transient ConsultationService consultationService;
  private final transient InvoiceService invoiceService;
  private final transient ServiceService serviceService;
  private final transient ProductService productService;
  private final transient ConsultationsForm consultationsForm;
  private final ConsultationDetailsSidebar detailsSidebar;


  public ConsultationsView(
      @Qualifier("consultationServiceImpl") ConsultationService consultationService,
      @Qualifier("employeeServiceImpl") EmployeeService employeeService,
      @Qualifier("petServiceImpl") PetService petService,
      @Qualifier("serviceServiceImpl") ServiceService serviceService,
      @Qualifier("productServiceImpl") ProductService productService,
      InvoiceService invoiceService) {
    this.consultationService = consultationService;
    this.invoiceService = invoiceService;
    this.productService = productService;
    this.serviceService = serviceService;
    this.consultationsForm =
        new ConsultationsForm(
            consultationService,
            employeeService,
            petService,
            serviceService,
            invoiceService,
            productService);
    this.detailsSidebar = new ConsultationDetailsSidebar(invoiceService, consultationsForm);
    add(detailsSidebar); // para que el overlay viva en el DOM


    setId("consultations-view");

    consultationsForm.setOnSaveCallback(
        saved -> {
          refreshAll();
          consultationsForm.close();
        });

    createGrid(consultationService, createFilterSpecification());

    final Div gridLayout = new Div(grid);
    gridLayout.addClassNames(
        LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Padding.SMALL, LumoUtility.Height.FULL);
    grid.addThemeVariants(
        GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);


    grid.asSingleSelect().addValueChangeListener(e -> {
      Consultation sel = e.getValue();
      if (sel != null) {
        detailsSidebar.open(sel);
      }
    });


    add(createTitle(), createFilter(), gridLayout);
    addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);
    setSizeFull();
    updateQuantity();

    create.addClickListener(event -> consultationsForm.openForNew());
  }

  private void createGrid(ConsultationService service, Specification<Consultation> specification) {
    GridUtils.configureGrid(grid, specification, service.getRepository());

    GridUtils.addColumn(
        grid, c -> c.getPet() != null ? c.getPet().getName() : "", "Mascota", "pet");
    GridUtils.addColumn(
        grid,
        c -> c.getVeterinarian() != null ? c.getVeterinarian().getFirstName() : "",
        "Veterinario",
        "veterinarian");
    GridUtils.addColumn(
        grid, Consultation::getConsultationDate, "Fecha de Consulta", "consultationDate");
    GridUtils.addColumn(grid, Consultation::getNotes, "Notas", "notes");
    GridUtils.addColumn(grid, Consultation::getDiagnosis, "Diagnóstico", "diagnosis");
    GridUtils.addColumn(grid, Consultation::getTreatment, "Tratamiento", "treatment");
    GridUtils.addColumn(grid, Consultation::getPrescription, "Prescripción", "prescription");
    GridUtils.addComponentColumn(grid, this::renderStatus, "Estado", "active");
    grid.addComponentColumn(this::createActionsColumn).setHeader("Acciones").setAutoWidth(true);
  }

  public Specification<Consultation> createFilterSpecification() {
    return (root, query, builder) -> {
      Order order = builder.desc(root.get("consultationDate"));
      if (query != null) {
        query.orderBy(order);
      }

      return builder.or(
          builder.like(
              builder.lower(root.get("notes")), "%" + searchField.getValue().toLowerCase() + "%"),
          builder.like(
              builder.lower(root.get("diagnosis")),
              "%" + searchField.getValue().toLowerCase() + "%"),
          builder.like(
              builder.lower(root.get("treatment")),
              "%" + searchField.getValue().toLowerCase() + "%"),
          builder.like(
              builder.lower(root.get("prescription")),
              "%" + searchField.getValue().toLowerCase() + "%"),
          builder.like(
              builder.lower(root.get("pet").get("name")),
              "%" + searchField.getValue().toLowerCase() + "%"),
          builder.like(
              builder.lower(root.get("veterinarian").get("firstName")),
              "%" + searchField.getValue().toLowerCase() + "%"),
          builder.like(
              builder.lower(root.get("veterinarian").get("lastName")),
              "%" + searchField.getValue().toLowerCase() + "%"));
    };
  }

  private Div createTitle() {
    final Breadcrumb breadcrumb = new Breadcrumb();
    breadcrumb.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
    breadcrumb.add(
        new BreadcrumbItem("Consultas", ConsultationsView.class),
        new BreadcrumbItem("Lista de Consultas", ConsultationsView.class));

    Icon icon = InfoIcon.INFO_CIRCLE.create("Gestionar consultas de la clínica veterinaria.");

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
    searchField.setPlaceholder("Buscar por notas, diagnóstico, tratamiento...");
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
    List<Consultation> consultations = consultationService.findAll(Pageable.unpaged()).getContent();
    String filter = searchField.getValue().trim().toLowerCase();

    if (!filter.isEmpty()) {
      consultations =
          consultations.stream()
              .filter(
                  c ->
                      (c.getNotes() != null && c.getNotes().toLowerCase().contains(filter))
                          || (c.getDiagnosis() != null
                              && c.getDiagnosis().toLowerCase().contains(filter))
                          || (c.getTreatment() != null
                              && c.getTreatment().toLowerCase().contains(filter))
                          || (c.getPrescription() != null
                              && c.getPrescription().toLowerCase().contains(filter))
                          || (c.getPet() != null && c.getPet().getId().toString().contains(filter))
                          || (c.getVeterinarian() != null
                              && c.getVeterinarian().getId().toString().contains(filter)))
              .toList();
    }
    grid.setItems(consultations);
    updateQuantity();
  }

  private Component createActionsColumn(Consultation consultation) {
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

    edit.addClickListener(e -> evaluateForEdit(consultation));
    delete.addClickListener(e -> showDeleteConfirmationDialog(consultation));

    HorizontalLayout actions = new HorizontalLayout(edit, delete);
    actions.setSpacing(true);
    actions.setPadding(false);
    actions.setMargin(false);
    actions.setWidth(null);

    if (!(UserUtils.hasEmployeeRole(EmployeeRole.CLINIC_MANAGER) || UserUtils.hasSystemRole(SystemRole.SYSTEM_ADMIN))) {
      delete.setVisible(false);
    }

    return actions;
  }

  public void evaluateForEdit(Consultation consultation) {
    try {
      Invoice invoice = invoiceService.findByConsultation(consultation);
      if (invoice != null) {
        if (invoice.getStatus() != InvoiceStatus.PENDING) {
          NotificationUtils.error(
              "No se puede editar la consulta porque su factura asociada no está en estado PENDIENTE.");
          refreshAll();
        } else {
          consultationsForm.openForEdit(consultation);
        }
      } else {
        NotificationUtils.error("No se encontró una factura asociada a esta consulta.");
        refreshAll();
      }
    } catch (Exception e) {
      NotificationUtils.error("La consulta esta corrupta porque no tiene factura asociada.");
    }
  }

  private void showDeleteConfirmationDialog(Consultation consultation) {
    Dialog confirmDialog = new Dialog();
    confirmDialog.setHeaderTitle("Confirmar eliminación");
    confirmDialog.setModal(true);
    confirmDialog.setWidth("400px");

    Span message =
        new Span(
            "¿Está seguro de que desea eliminar la consulta de la mascota \""
                + (consultation.getPet() != null ? consultation.getPet().getName() : "")
                + "\"? Esta acción no se puede deshacer.");
    message.getStyle().set("margin-bottom", "20px");

    Button confirmButton = new Button("Eliminar");
    confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
    confirmButton.addClickListener(
        e -> {
          try {
            NotificationUtils.error("Favor solicite al manager que borre la factura.");
            // refreshAll();
            confirmDialog.close();
          } catch (Exception ex) {
            NotificationUtils.error("Error al eliminar la consulta: " + ex.getMessage());
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

  private Component renderStatus(Consultation consultation) {
    boolean isActive = consultation.isActive();
    Span badge = new Span(isActive ? "Activo" : "Inactivo");
    badge.getElement().getThemeList().add("badge pill");
    badge.getElement().getThemeList().add(isActive ? "success" : "error");
    return badge;
  }

  private void updateQuantity() {
    try {
      long count = consultationService.getRepository().count();
      quantity.setText("Consultas (" + count + ")");
    } catch (Exception e) {
      log.warn("Error getting consultations count", e);
      quantity.setText("Consultas");
    }
  }


}
