package com.wornux.views.consultations;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.components.Sidebar;
import com.wornux.data.entity.*;
import com.wornux.data.enums.InvoiceStatus;
import com.wornux.services.implementations.InvoiceService;
import com.wornux.utils.NotificationUtils;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ConsultationDetailsSidebar extends Div {

  private final Sidebar sidebar = new Sidebar();
  private final InvoiceService invoiceService;
  private final ConsultationsForm consultationsForm;

  private final Div headerArea = new Div(); // badges
  private final Div infoArea = new Div(); // cliente/mascota
  private final Div detailsArea = new Div(); // detalles de la consulta
  private final Div footerArea = new Div(); // totales

  private Consultation current;

  public ConsultationDetailsSidebar(
      InvoiceService invoiceService, ConsultationsForm consultationsForm) {
    this.invoiceService = invoiceService;
    this.consultationsForm = consultationsForm;

    // ancho tipo "drawer"
    sidebar.setMinWidth("50vw");
    sidebar.setWidth("60vw");
    sidebar.setMaxWidth("1200px");

    headerArea.add(buildHeaderBadges(null, null));

    infoArea.addClassNames(LumoUtility.Padding.SMALL, LumoUtility.Gap.SMALL);
    detailsArea.addClassNames(LumoUtility.Padding.SMALL, LumoUtility.Gap.SMALL);
    footerArea.addClassNames(LumoUtility.Padding.SMALL, LumoUtility.Gap.MEDIUM);

    sidebar.createHeaderContent(headerArea);
    sidebar.createContent(headerArea, infoArea, detailsArea, footerArea);
    sidebar.addClassNames(LumoUtility.Width.FULL);
    sidebar.addSubTitle("Información de la consulta");

    // botones
    sidebar.getSave().setText("Editar consulta");
    sidebar.getDelete().getElement().getStyle().set("display", "none");
    sidebar.getSave().addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    sidebar.setOnSaveClickListener(
        e -> {
          if (current == null) {
            sidebar.close();
            return;
          }
          try {
            Invoice inv = invoiceService.findByConsultation(current);
            if (inv != null && inv.getStatus() == InvoiceStatus.PENDING) {
              consultationsForm.openForEdit(current);
            } else if (inv != null) {
              NotificationUtils.error(
                  "La factura está " + inv.getStatus() + ". Solo se edita si está PENDING.");
            } else {
              NotificationUtils.error("No se encontró una factura asociada a esta consulta.");
            }
          } catch (Exception ex) {
            NotificationUtils.error("No fue posible validar la factura asociada.");
          }
          sidebar.close();
        });

    sidebar.getCancel().setText("Cerrar");
    sidebar.setOnCancelClickListener(e -> sidebar.close());

    add(sidebar);
    setWidthFull();
  }

  public void open(Consultation c) {
    this.current = c;

    Invoice inv = null;
    try {
      inv = invoiceService.findByConsultationIdWithDetails(c.getId()).orElse(null);
      if (inv == null) {
        inv = invoiceService.findByConsultation(c);
      }
    } catch (Exception ignore) {
    }

    headerArea.removeAll();
    headerArea.add(buildHeaderBadges(c, inv));

    infoArea.removeAll();
    infoArea.add(buildClientAndPet(c));

    detailsArea.removeAll();
    detailsArea.add(buildConsultationDetails(c));
    detailsArea.add(new Hr());
    detailsArea.add(new H4("Resumen de cargos"));

    var lines = buildLines(inv);
    detailsArea.add(lines.grid);

    footerArea.removeAll();
    footerArea.add(lines.totals);

    sidebar.createContent(headerArea, infoArea, detailsArea, footerArea);
    sidebar.editObject("Detalles de la Consulta");
  }

  /* ===== helpers ===== */

  private Component buildHeaderBadges(Consultation c, Invoice inv) {
    Span status = new Span((c != null && c.isActive()) ? "ACTIVO" : "INACTIVO");
    status.getElement().getThemeList().add("badge pill");
    status.getElement().getThemeList().add((c != null && c.isActive()) ? "success" : "error");

    Span invBadge = new Span("FACTURA: N/A");
    invBadge.getElement().getThemeList().add("badge pill");
    invBadge.getElement().getThemeList().add("contrast");
    if (inv != null) {
      invBadge.setText("FACTURA: " + inv.getStatus().name());
      invBadge.getElement().getThemeList().remove("contrast");
      switch (inv.getStatus()) {
        case PAID -> invBadge.getElement().getThemeList().add("success");
        case PENDING -> invBadge.getElement().getThemeList().add("primary");
        default -> invBadge.getElement().getThemeList().add("error");
      }
    }

    HorizontalLayout hl = new HorizontalLayout(status, invBadge);
    hl.setWidthFull();
    hl.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    return hl;
  }

  private Component buildClientAndPet(Consultation c) {
    Client owner = null;
    if (c.getPet() != null) {
      List<Client> owners =
          (c.getPet().getOwners() != null) ? c.getPet().getOwners() : Collections.emptyList();
      owner = owners.stream().findFirst().orElse(null);
    }

    Span clientName =
        new Span(
            owner != null
                ? owner.getFirstName() + " " + owner.getLastName()
                : "Sin dueño asignado");
    clientName.getElement().getStyle().set("font-weight", "bold").set("font-size", "1.1rem");

    Icon phoneIcon = VaadinIcon.PHONE.create();
    phoneIcon.setColor("var(--lumo-secondary-text-color)");
    Span phoneText =
        new Span(owner != null && owner.getPhoneNumber() != null ? owner.getPhoneNumber() : "N/A");

    Icon mailIcon = VaadinIcon.ENVELOPE.create();
    mailIcon.setColor("var(--lumo-secondary-text-color)");
    Span emailText = new Span(owner != null && owner.getEmail() != null ? owner.getEmail() : "N/A");

    HorizontalLayout contact =
        new HorizontalLayout(phoneIcon, phoneText, new Span("•"), mailIcon, emailText);
    contact.setAlignItems(FlexComponent.Alignment.CENTER);

    Span petInfo =
        new Span(
            (c.getPet() != null ? c.getPet().getName() : "N/A")
                + " • "
                + (c.getPet() != null && c.getPet().getType() != null
                    ? c.getPet().getType().name()
                    : "N/A")
                + " • "
                + (c.getPet() != null && c.getPet().getBreed() != null
                    ? c.getPet().getBreed()
                    : "N/A")
                + " • "
                + (c.getPet() != null && c.getPet().getGender() != null
                    ? c.getPet().getGender().name()
                    : "N/A"));
    petInfo
        .getElement()
        .getStyle()
        .set("font-weight", "600")
        .set("color", "var(--lumo-primary-text-color)");

    Div box = new Div(clientName, contact, petInfo, new Hr());
    box.addClassNames(
        LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Gap.SMALL);
    return box;
  }

  private Component buildConsultationDetails(Consultation c) {
    // Veterinario
    Icon vetIcon = VaadinIcon.USER.create();
    vetIcon.setColor("var(--lumo-secondary-text-color)");
    String vetName =
        (c.getVeterinarian() != null)
            ? c.getVeterinarian().getFirstName() + " " + c.getVeterinarian().getLastName()
            : "N/A";
    HorizontalLayout vetRow = new HorizontalLayout(vetIcon, new Span("Veterinario: " + vetName));

    // Fecha
    Icon dateIcon = VaadinIcon.CALENDAR.create();
    dateIcon.setColor("var(--lumo-secondary-text-color)");
    String dateStr =
        c.getConsultationDate() != null
            ? c.getConsultationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a"))
            : "N/A";
    HorizontalLayout dateRow =
        new HorizontalLayout(dateIcon, new Span("Fecha de consulta: " + dateStr));

    // Notas, Diagnóstico, Tratamiento, Prescripción
    Icon notesIcon = VaadinIcon.NOTEBOOK.create();
    notesIcon.setColor("var(--lumo-secondary-text-color)");
    HorizontalLayout notesRow =
        new HorizontalLayout(
            notesIcon,
            new Span(
                "Notas: "
                    + (c.getNotes() != null && !c.getNotes().isBlank() ? c.getNotes() : "N/A")));

    Icon diagIcon = VaadinIcon.CLIPBOARD_HEART.create();
    diagIcon.setColor("var(--lumo-secondary-text-color)");
    HorizontalLayout diagRow =
        new HorizontalLayout(
            diagIcon,
            new Span(
                "Diagnóstico: "
                    + (c.getDiagnosis() != null && !c.getDiagnosis().isBlank()
                        ? c.getDiagnosis()
                        : "N/A")));

    Icon treatIcon = VaadinIcon.STETHOSCOPE.create();
    treatIcon.setColor("var(--lumo-secondary-text-color)");
    HorizontalLayout treatRow =
        new HorizontalLayout(
            treatIcon,
            new Span(
                "Tratamiento: "
                    + (c.getTreatment() != null && !c.getTreatment().isBlank()
                        ? c.getTreatment()
                        : "N/A")));

    Icon rxIcon = VaadinIcon.PILL.create();
    rxIcon.setColor("var(--lumo-secondary-text-color)");
    HorizontalLayout rxRow =
        new HorizontalLayout(
            rxIcon,
            new Span(
                "Prescripción: "
                    + (c.getPrescription() != null && !c.getPrescription().isBlank()
                        ? c.getPrescription()
                        : "N/A")));

    for (HorizontalLayout row :
        new HorizontalLayout[] {vetRow, dateRow, notesRow, diagRow, treatRow, rxRow}) {
      row.setAlignItems(FlexComponent.Alignment.CENTER);
      row.setSpacing(true);
    }

    Div box = new Div(vetRow, dateRow, notesRow, diagRow, treatRow, rxRow);
    box.addClassNames(
        LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Gap.SMALL);
    return box;
  }

  private record LinesAndTotals(Grid<LineRow> grid, HorizontalLayout totals) {}

  private LinesAndTotals buildLines(Invoice inv) {
    Grid<LineRow> grid = new Grid<>(LineRow.class, false);
    grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    grid.setAllRowsVisible(true);
    grid.setWidthFull();

    grid.addColumn(LineRow::tipo).setHeader("Tipo").setAutoWidth(true);
    grid.addColumn(LineRow::nombre).setHeader("Concepto").setAutoWidth(true);
    grid.addColumn(r -> String.format("%.2f", r.cantidad())).setHeader("Cant.").setAutoWidth(true);
    grid.addColumn(r -> toMoney(r.precio())).setHeader("Precio").setTextAlign(ColumnTextAlign.END);
    grid.addColumn(r -> toMoney(r.importe()))
        .setHeader("Importe")
        .setTextAlign(ColumnTextAlign.END);

    List<LineRow> rows = new ArrayList<>();
    BigDecimal subtotal = BigDecimal.ZERO, tax = BigDecimal.ZERO, total = BigDecimal.ZERO;

    if (inv != null) {
      if (inv.getOfferings() != null) {
        rows.addAll(
            inv.getOfferings().stream()
                .map(
                    si ->
                        new LineRow(
                            "Servicio",
                            si.getOffering() != null ? si.getOffering().getName() : "",
                            Optional.ofNullable(si.getQuantity()).orElse(0.0),
                            Optional.ofNullable(si.getOffering().getPrice()).orElse(BigDecimal.ZERO),
                            Optional.ofNullable(si.getAmount()).orElse(BigDecimal.ZERO)))
                .collect(Collectors.toList()));
      }
      if (inv.getProducts() != null) {
        rows.addAll(
            inv.getProducts().stream()
                .map(
                    ip ->
                        new LineRow(
                            "Producto",
                            ip.getProduct() != null ? ip.getProduct().getName() : "",
                            Optional.ofNullable(ip.getQuantity()).orElse(0.0),
                            Optional.ofNullable(ip.getPrice()).orElse(BigDecimal.ZERO),
                            Optional.ofNullable(ip.getAmount()).orElse(BigDecimal.ZERO)))
                .collect(Collectors.toList()));
      }

      subtotal = Optional.ofNullable(inv.getSubtotal()).orElse(BigDecimal.ZERO);
      tax = Optional.ofNullable(inv.getTax()).orElse(BigDecimal.ZERO);
      total = Optional.ofNullable(inv.getTotal()).orElse(BigDecimal.ZERO);
    }

    grid.setItems(rows);

    HorizontalLayout totals =
        new HorizontalLayout(
            new Span("Subtotal: " + toMoney(subtotal)),
            new Span("Impuestos: " + toMoney(tax)),
            new Span("Total: " + toMoney(total)));
    totals.setWidthFull();
    totals.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

    return new LinesAndTotals(grid, totals);
  }

  private static String toMoney(BigDecimal v) {
    return "$" + (v == null ? "0.00" : v.setScale(2, BigDecimal.ROUND_HALF_UP));
  }

  private record LineRow(
      String tipo, String nombre, Double cantidad, BigDecimal precio, BigDecimal importe) {}
}
