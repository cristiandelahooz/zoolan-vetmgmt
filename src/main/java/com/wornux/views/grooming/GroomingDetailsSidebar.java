package com.wornux.views.grooming;

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
import com.wornux.services.implementations.InvoiceService;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class GroomingDetailsSidebar extends Div {

  private final Sidebar sidebar = new Sidebar();
  private final InvoiceService invoiceService;
  private final GroomingForm groomingForm;

  private final Div headerArea = new Div(); // badges + título secundario si quieres
  private final Div infoArea = new Div(); // cliente / mascota
  private final Div detailsArea = new Div(); // detalles sesión
  private final Div linesArea = new Div(); // resumen cargos (grid)
  private final Div footerArea = new Div(); // totales

  private GroomingSession current;

  public GroomingDetailsSidebar(InvoiceService invoiceService, GroomingForm groomingForm) {
    this.invoiceService = invoiceService;
    this.groomingForm = groomingForm;

    sidebar.setMinWidth("50vw");
    sidebar.setWidth("60vw"); // o 65vw si quieres más
    sidebar.setMaxWidth("1200px");

    // header "tabs" del Sidebar (puede ser un div simple)
    headerArea.add(buildHeaderBadges(null, null)); // se actualiza en open()

    // contenido principal
    infoArea.addClassNames(LumoUtility.Padding.SMALL, LumoUtility.Gap.SMALL);
    detailsArea.addClassNames(LumoUtility.Padding.SMALL, LumoUtility.Gap.SMALL);
    linesArea.addClassNames(LumoUtility.Padding.SMALL, LumoUtility.Gap.SMALL);
    footerArea.addClassNames(LumoUtility.Padding.SMALL, LumoUtility.Gap.MEDIUM);

    // montar el Sidebar (misma API que en InvoiceForm)
    sidebar.createHeaderContent(headerArea);
    sidebar.createContent(headerArea, infoArea, detailsArea, footerArea); // 4 slots
    sidebar.addClassNames(LumoUtility.Width.FULL);
    sidebar.addSubTitle("Información de la sesión");

    // botones del Sidebar
    // Ocultar el botón Eliminar
    sidebar.getDelete().getElement().getStyle().set("display", "none");
    sidebar.getSave().setText("Editar grooming");
    sidebar.getSave().addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    sidebar.setOnSaveClickListener(
        e -> {
          if (current != null) groomingForm.openForEdit(current);
          sidebar.close();
        });

    sidebar.getCancel().setText("Cerrar");
    sidebar.setOnCancelClickListener(e -> sidebar.close());

    add(sidebar);
    setWidthFull();
  }

  public void open(GroomingSession s) {
    this.current = s;

    // Factura con detalle (si existe)
    Invoice inv =
        invoiceService
            .findByGroomingIdWithDetails(s.getId())
            .orElseGet(() -> invoiceService.findByGroomingId(s.getId()).orElse(null));

    // Header badges
    headerArea.removeAll();
    headerArea.add(buildHeaderBadges(s, inv));

    // Bloque: Cliente + Mascota
    infoArea.removeAll();
    infoArea.add(buildClientAndPet(s));

    // Bloque: Detalles de la sesión
    detailsArea.removeAll();
    detailsArea.add(buildSessionDetails(s));

    // Bloque: Resumen de cargos
    linesArea.removeAll();
    var linesAndTotals = buildLines(inv);
    detailsArea.add(new Hr());
    detailsArea.add(new H4("Resumen de cargos"));
    detailsArea.add(linesAndTotals.grid);
    footerArea.removeAll();
    footerArea.add(linesAndTotals.totals);

    // Inyectar el cuarto bloque (footer de totales) en el Sidebar
    sidebar.createContent(headerArea, infoArea, detailsArea, footerArea);

    sidebar.editObject("Detalles de Grooming");
  }

  // ===== helpers =====

  private Component buildHeaderBadges(GroomingSession s, Invoice inv) {
    Span status =
        new Span((s != null && Boolean.TRUE.equals(s.getActive())) ? "ACTIVO" : "INACTIVO");
    status.getElement().getThemeList().add("badge pill");
    status
        .getElement()
        .getThemeList()
        .add((s != null && Boolean.TRUE.equals(s.getActive())) ? "success" : "error");

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

  private Component buildClientAndPet(GroomingSession s) {
    Client owner = null;
    if (s.getPet() != null) {
      List<Client> owners =
          (s.getPet().getOwners() != null) ? s.getPet().getOwners() : Collections.emptyList();
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
            (s.getPet() != null ? s.getPet().getName() : "N/A")
                + " • "
                + (s.getPet() != null && s.getPet().getType() != null
                    ? s.getPet().getType().name()
                    : "N/A")
                + " • "
                + (s.getPet() != null && s.getPet().getBreed() != null
                    ? s.getPet().getBreed()
                    : "N/A")
                + " • "
                + (s.getPet() != null && s.getPet().getGender() != null
                    ? s.getPet().getGender().name()
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

  private Component buildSessionDetails(GroomingSession s) {
    Icon groomerIcon = VaadinIcon.USER.create();
    groomerIcon.setColor("var(--lumo-secondary-text-color)");
    String groomerName =
        (s.getGroomer() != null)
            ? s.getGroomer().getFirstName() + " " + s.getGroomer().getLastName()
            : "N/A";
    HorizontalLayout groomerRow =
        new HorizontalLayout(groomerIcon, new Span("Groomer: " + groomerName));

    Icon dateIcon = VaadinIcon.CALENDAR.create();
    dateIcon.setColor("var(--lumo-secondary-text-color)");
    String dateStr =
        s.getGroomingDate() != null
            ? s.getGroomingDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a"))
            : "N/A";
    HorizontalLayout dateRow =
        new HorizontalLayout(dateIcon, new Span("Fecha de grooming: " + dateStr));

    Icon notesIcon = VaadinIcon.NOTEBOOK.create();
    notesIcon.setColor("var(--lumo-secondary-text-color)");
    HorizontalLayout notesRow =
        new HorizontalLayout(
            notesIcon,
            new Span(
                "Notas: "
                    + (s.getNotes() != null && !s.getNotes().isBlank() ? s.getNotes() : "N/A")));

    for (HorizontalLayout row : new HorizontalLayout[] {groomerRow, dateRow, notesRow}) {
      row.setAlignItems(FlexComponent.Alignment.CENTER);
      row.setSpacing(true);
    }

    Div box = new Div(groomerRow, dateRow, notesRow);
    box.addClassNames(
        LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Gap.SMALL);
    return box;
  }

  private record LinesAndTotals(Grid<LineRow> grid, HorizontalLayout totals) {}

  private LinesAndTotals buildLines(Invoice inv) {
    Grid<LineRow> grid = new Grid<>(LineRow.class, false);
    grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    grid.setAllRowsVisible(true);

    grid.addColumn(LineRow::tipo).setHeader("Tipo").setAutoWidth(true);
    grid.addColumn(LineRow::nombre).setHeader("Concepto").setAutoWidth(true);
    grid.addColumn(r -> String.format("%.2f", r.cantidad())).setHeader("Cant.").setAutoWidth(true);
    grid.addColumn(r -> toMoney(r.precio())).setHeader("Precio").setTextAlign(ColumnTextAlign.END);
    ;
    grid.addColumn(r -> toMoney(r.importe()))
        .setHeader("Importe")
        .setTextAlign(ColumnTextAlign.END);
    ;

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
