package com.wornux.views.waitingroom;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.wornux.data.entity.Employee;
import com.wornux.data.entity.WaitingRoom;
import com.wornux.data.enums.EmployeeRole;
import com.wornux.data.enums.WaitingRoomStatus;
import com.wornux.security.UserUtils;
import com.wornux.services.implementations.InvoiceService;
import com.wornux.services.interfaces.*;
import com.wornux.utils.NotificationUtils;
import com.wornux.views.MainLayout;
import com.wornux.views.consultations.ConsultationsForm;
import com.wornux.views.grooming.GroomingForm;
import jakarta.annotation.security.RolesAllowed;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "mi-sala-espera", layout = MainLayout.class)
@PageTitle("Mi Sala de Espera")
// 👇 permite que entren VETERINARIAN y/o GROOMER
@RolesAllowed({"ROLE_EMP_VETERINARIAN", "ROLE_EMP_GROOMER"})
public class MyWaitingRoomView extends VerticalLayout {

  private final WaitingRoomService waitingRoomService;
  private final ConsultationService consultationService;
  private final GroomingSessionService groomingSessionService; // 👈 nuevo
  private final EmployeeService employeeService;
  private final PetService petService;
  private final InvoiceService invoiceService;
  private final ProductService productService;
  private final ServiceService serviceService;

  private final Grid<WaitingRoom> grid = new Grid<>(WaitingRoom.class, false);

  public MyWaitingRoomView(
      WaitingRoomService waitingRoomService,
      ServiceService serviceService,
      ConsultationService consultationService,
      GroomingSessionService groomingSessionService, // 👈 inyecta
      EmployeeService employeeService,
      PetService petService,
      InvoiceService invoiceService,
      ProductService productService) {

    this.waitingRoomService = waitingRoomService;
    this.consultationService = consultationService;
    this.groomingSessionService = groomingSessionService; // 👈 guarda ref
    this.employeeService = employeeService;
    this.petService = petService;
    this.invoiceService = invoiceService;
    this.productService = productService;
    this.serviceService = serviceService;

    setSizeFull();
    setPadding(true);
    setSpacing(true);

    grid.addColumn(wr -> wr.getPet().getName()).setHeader("Mascota");
    grid.addColumn(wr -> wr.getClient().getFirstName() + " " + wr.getClient().getLastName())
        .setHeader("Cliente");
    grid.addColumn(WaitingRoom::getReasonForVisit).setHeader("Motivo");
    grid.addColumn(wr -> wr.getStatus().name()).setHeader("Estado");

    // Acción contextual: si es vet → abrir consulta; si es groomer → iniciar/atender grooming
    grid.addComponentColumn(
            wr -> {
              Button open = new Button(resolveActionLabel());
              open.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
              open.addClickListener(
                  e -> {
                    if (isVet()) openConsultationForm(wr);
                    else if (isGroomer()) openGroomingFlow(wr);
                  });
              return open;
            })
        .setHeader("Acción");

    // Al hacer click en una fila, abre el diálogo de detalles (mismo para ambos)
    grid.asSingleSelect()
        .addValueChangeListener(
            event -> {
              WaitingRoom selected = event.getValue();
              if (selected != null) {
                openDetailsDialog(selected);
              }
            });

    add(grid);
    refreshGrid();
  }

  private boolean isVet() {
    Employee me = UserUtils.getCurrentEmployee().orElse(null);
    return me != null && me.getEmployeeRole() == EmployeeRole.VETERINARIAN;
  }

  private boolean isGroomer() {
    Employee me = UserUtils.getCurrentEmployee().orElse(null);
    return me != null && me.getEmployeeRole() == EmployeeRole.GROOMER;
  }

  private String resolveActionLabel() {
    if (isVet()) return "Atender";
    if (isGroomer()) return "Grooming";
    return "Abrir";
  }

  private void refreshGrid() {
    Employee me = UserUtils.getCurrentEmployee().orElse(null);
    if (me == null) {
      NotificationUtils.error("No se pudo identificar al usuario logueado.");
      return;
    }

    List<WaitingRoom> items;

    if (isVet()) {
      // Usa el método del servicio que ya tienes para vet
      items = waitingRoomService.findForVeterinarian(me.getId());
    } else if (isGroomer()) {
      // 👇 espejo para groomer (debe existir en tu servicio)
      items = waitingRoomService.findForGroomer(me.getId());
    } else {
      items = List.of();
    }

    // (opcional) Si tu findForX ya filtra por estados activos no necesitas este filtro:
    items =
        items.stream()
            .filter(
                wr ->
                    wr.getStatus() == WaitingRoomStatus.ESPERANDO
                        || wr.getStatus() == WaitingRoomStatus.EN_PROCESO)
            .toList();

    grid.setItems(items);
  }

  // === Flujo VETERINARIO (igual al tuyo) ===
  private void openConsultationForm(WaitingRoom wr) {
    Employee currentVet = UserUtils.getCurrentEmployee().orElse(null);
    if (currentVet == null) {
      NotificationUtils.error("No se pudo identificar al veterinario logueado.");
      return;
    }

    ConsultationsForm form =
        new ConsultationsForm(
            consultationService,
            employeeService,
            petService,
            serviceService,
            invoiceService,
            productService);
    form.openForNew();

    // bloquear vet (siempre en esta vista)
    form.presetForVetAndPet(wr.getPet(), currentVet, true);

    // pasar la entrada de waiting room para que el form la cierre al guardar
    form.attachWaitingRoom(wr);

    form.setOnSaveCallback(
        c -> {
          NotificationUtils.success("Consulta finalizada para " + wr.getPet().getName());
          refreshGrid();
        });

    form.open();
  }

  // === Flujo GROOMER (simple y sin duplicar vista) ===
  // Si ya tienes un formulario de grooming, reemplaza el cuerpo por abrir ese diálogo.
  /*private void openGroomingFlow(WaitingRoom wr) {
    Employee currentGroomer = UserUtils.getCurrentEmployee().orElse(null);
    if (currentGroomer == null) {
      NotificationUtils.error("No se pudo identificar al groomer logueado.");
      return;
    }
    try {
      // Si aún no está en proceso, se inicia. Tu servicio ya hace:
      // - wr.startService() → EN_PROCESO
      // - crear GroomingSession activa con pet/groomer/fecha
      groomingSessionService.start(wr.getId());
      NotificationUtils.success("Grooming iniciado para " + wr.getPet().getName());
      refreshGrid();
    } catch (Exception ex) {
      NotificationUtils.error("No se pudo iniciar grooming: " + ex.getMessage());
    }
  }*/

  private void openGroomingFlow(WaitingRoom wr) {
    Employee currentGroomer = UserUtils.getCurrentEmployee().orElse(null);
    if (currentGroomer == null) {
      NotificationUtils.error("No se pudo identificar al groomer logueado.");
      return;
    }

    GroomingForm form =
        new GroomingForm(
            groomingSessionService,
            employeeService,
            petService,
            serviceService,
            invoiceService,
            productService);

    form.openForNew();

    // bloquear groomer y mascota
    form.presetForGroomerAndPet(wr.getPet(), currentGroomer, true);

    // pasar la WR para que el form, al guardar, haga finish() y cierre la entrada
    form.attachWaitingRoom(wr);

    form.setOnSaveCallback(
        s -> {
          NotificationUtils.success("Grooming finalizado para " + wr.getPet().getName());
          refreshGrid();
        });

    form.open();
  }

  // === Diálogo de detalles (reutilizable para ambas áreas) ===
  private void openDetailsDialog(WaitingRoom wr) {
    Dialog dialog = new Dialog();
    dialog.setHeaderTitle("Detalles de la atención");
    dialog.setModal(true);
    dialog.setDraggable(true);
    dialog.setResizable(true);
    dialog.setWidth("600px");

    VerticalLayout layout = new VerticalLayout();
    layout.setWidthFull();
    layout
        .getStyle()
        .set("padding", "1.5rem")
        .set("border-radius", "10px")
        .set("background-color", "var(--lumo-base-color)")
        .set("box-shadow", "var(--lumo-box-shadow-m)");
    layout.setSpacing(true);
    layout.setPadding(false);

    // Prioridad + Estado
    Span priority = new Span(wr.getPriority().name());
    priority.getElement().getThemeList().add("badge pill");
    switch (wr.getPriority()) {
      case URGENTE -> priority.getElement().getThemeList().add("error");
      case EMERGENCIA -> priority.getElement().getThemeList().add("warning");
      default -> priority.getElement().getThemeList().add("success");
    }

    Span status = new Span(wr.getStatus().name().replace("_", " "));
    status.getElement().getThemeList().add("badge pill");
    switch (wr.getStatus()) {
      case EN_PROCESO -> status.getElement().getThemeList().add("success");
      case COMPLETADO -> status.getElement().getThemeList().add("contrast");
      case CANCELADO -> status.getElement().getThemeList().add("error");
      default -> status.getElement().getThemeList().add("primary");
    }

    HorizontalLayout header = new HorizontalLayout(priority, status);
    header.setWidthFull();
    header.setJustifyContentMode(JustifyContentMode.BETWEEN);

    // Cliente
    Span clientName = new Span(wr.getClient().getFirstName() + " " + wr.getClient().getLastName());
    clientName.getElement().getStyle().set("font-weight", "bold").set("font-size", "1.2em");

    Icon phoneIcon = VaadinIcon.PHONE.create();
    phoneIcon.setColor("var(--lumo-secondary-text-color)");
    Span phoneText = new Span(wr.getClient().getPhoneNumber());
    Icon mailIcon = VaadinIcon.ENVELOPE.create();
    mailIcon.setColor("var(--lumo-secondary-text-color)");
    Span emailText = new Span(wr.getClient().getEmail());

    HorizontalLayout contactInfo =
        new HorizontalLayout(phoneIcon, phoneText, new Span("•"), mailIcon, emailText);
    contactInfo.setAlignItems(FlexComponent.Alignment.CENTER);
    contactInfo.setSpacing(true);

    // Mascota
    Span petInfo =
        new Span(
            wr.getPet().getName()
                + " • "
                + wr.getPet().getType().name()
                + " • "
                + wr.getPet().getBreed()
                + " • "
                + wr.getPet().getGender());
    petInfo
        .getElement()
        .getStyle()
        .set("font-weight", "600")
        .set("color", "var(--lumo-primary-text-color)")
        .set("font-size", "1.05em");

    // Datos de visita
    Icon reasonIcon = VaadinIcon.CLIPBOARD_TEXT.create();
    reasonIcon.setColor("var(--lumo-secondary-text-color)");
    Span reasonText = new Span("Motivo: " + wr.getReasonForVisit());
    HorizontalLayout reason = new HorizontalLayout(reasonIcon, reasonText);

    Icon notesIcon = VaadinIcon.NOTEBOOK.create();
    notesIcon.setColor("var(--lumo-secondary-text-color)");
    Span notesText =
        new Span(
            "Notas: "
                + (wr.getNotes() != null && !wr.getNotes().isBlank() ? wr.getNotes() : "N/A"));
    HorizontalLayout notes = new HorizontalLayout(notesIcon, notesText);

    Icon arrivalIcon = VaadinIcon.CLOCK.create();
    arrivalIcon.setColor("var(--lumo-secondary-text-color)");
    Span arrivalText =
        new Span(
            "Hora de llegada: "
                + wr.getArrivalTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
    HorizontalLayout arrival = new HorizontalLayout(arrivalIcon, arrivalText);

    Duration waitTime = Duration.ZERO;
    if (wr.getArrivalTime() != null && !wr.getArrivalTime().isAfter(LocalDateTime.now())) {
      waitTime = Duration.between(wr.getArrivalTime(), LocalDateTime.now());
    }
    String formattedWait = String.format("%dh %02dm", waitTime.toHours(), waitTime.toMinutesPart());

    Icon waitIcon = VaadinIcon.HOURGLASS.create();
    waitIcon.setColor("var(--lumo-secondary-text-color)");
    Span waitingText = new Span("Esperando: " + formattedWait);
    HorizontalLayout waiting = new HorizontalLayout(waitIcon, waitingText);

    // Sólo botón “Cancelar” (como pediste)
    Button cancelBtn = new Button("Cancelar atención");
    cancelBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
    cancelBtn.setMinWidth("200px");

    cancelBtn.addClickListener(
        e -> {
          ConfirmDialog dialogConfirm = new ConfirmDialog();
          dialogConfirm.setHeader("¿Estás seguro?");
          dialogConfirm.setText("Esto cancelará la atención y marcará la entrada como cancelada.");
          dialogConfirm.setCancelable(true);
          dialogConfirm.setCancelText("No");
          dialogConfirm.setConfirmText("Sí, cancelar");

          dialogConfirm.addConfirmListener(
              ev -> {
                if (wr.getStatus() == WaitingRoomStatus.EN_PROCESO) {
                  NotificationUtils.error("No se puede cancelar una entrada en proceso.");
                  return;
                }
                waitingRoomService.delete(wr.getId());
                refreshGrid();
                dialog.close();
              });

          dialogConfirm.open();
        });

    HorizontalLayout buttons = new HorizontalLayout(cancelBtn);
    buttons.setSpacing(true);
    buttons.setJustifyContentMode(JustifyContentMode.CENTER);
    buttons.setWidthFull();
    buttons.getStyle().set("margin-top", "1rem");

    layout.add(header, clientName, contactInfo, petInfo, reason, notes, arrival, waiting, buttons);
    dialog.add(layout);
    dialog.open();
  }
}
