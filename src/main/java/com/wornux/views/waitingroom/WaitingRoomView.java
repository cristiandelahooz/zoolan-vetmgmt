package com.wornux.views.waitingroom;

import static com.wornux.utils.PredicateUtils.createPredicateForSelectedItems;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.Employee;
import com.wornux.data.entity.WaitingRoom;
import com.wornux.data.enums.Priority;
import com.wornux.data.enums.WaitingRoomStatus;
import com.wornux.services.interfaces.*;
import com.wornux.utils.GridUtils;
import com.wornux.utils.NotificationUtils;
import com.wornux.views.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.criteria.Predicate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

@Slf4j
@Route(value = "sala-espera", layout = MainLayout.class)
@PageTitle("Sala de Espera")
@RolesAllowed({"ROLE_SYSTEM_ADMIN", "ROLE_MANAGER", "ROLE_EMP_RECEPTIONIST"})
public class WaitingRoomView extends VerticalLayout {

  private final WaitingRoomService waitingRoomService;
  private final ClientService clientService;
  private final PetService petService;

  private final EmployeeService employeeService;
  private final ConsultationService consultationService;

  private final Grid<WaitingRoom> grid = GridUtils.createBasicGrid(WaitingRoom.class);
  private final WaitingRoomForm form;
  private final VerticalLayout cardContainer = new VerticalLayout();

  private final MultiSelectComboBox<Priority> priorityFilter =
      new MultiSelectComboBox<>("Prioridad");
  private final MultiSelectComboBox<WaitingRoomStatus> statusFilter =
      new MultiSelectComboBox<>("Estado");
  private final Span quantity = new Span();
  TextField searchField = new TextField();

  public WaitingRoomView(
      WaitingRoomService waitingRoomService,
      ClientService clientService,
      PetService petService,
      ConsultationService consultationService,
      EmployeeService employeeService) {
    this.waitingRoomService = waitingRoomService;
    this.clientService = clientService;
    this.petService = petService;
    this.consultationService = consultationService;
    this.employeeService = employeeService;

    setSizeFull();
    setPadding(true);
    setSpacing(true);
    setClassName(LumoUtility.Padding.LARGE);

    Span title = new Span("Sala de Espera");
    title.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.XLARGE);

    Icon infoIcon = VaadinIcon.INFO_CIRCLE_O.create();
    infoIcon.getStyle().set("cursor", "pointer").set("color", "var(--lumo-primary-color)");
    infoIcon
        .getElement()
        .setProperty(
            "title", "Aquí puedes gestionar las mascotas que están esperando ser atendidas.");

    HorizontalLayout titleWithInfo = new HorizontalLayout(title, infoIcon);
    titleWithInfo.setAlignItems(Alignment.CENTER);
    titleWithInfo.setSpacing(true);

    priorityFilter.setItems(Priority.values());
    priorityFilter.setClearButtonVisible(true);
    priorityFilter.setAutoExpand(MultiSelectComboBox.AutoExpandMode.BOTH);
    priorityFilter.addValueChangeListener(e -> refreshGrid());

    statusFilter.setItems(WaitingRoomStatus.values());
    statusFilter.setClearButtonVisible(true);
    statusFilter.setAutoExpand(MultiSelectComboBox.AutoExpandMode.BOTH);
    statusFilter.addValueChangeListener(e -> refreshGrid());

    searchField.setClearButtonVisible(true);
    searchField.setPlaceholder("Buscar por cliente o mascota...");
    searchField.setWidth("50%");
    searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
    searchField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.EAGER);
    searchField.addValueChangeListener(e -> refreshGrid());

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
    updateQuantity();

    statusFilter.setItems(WaitingRoomStatus.values());
    statusFilter.setClearButtonVisible(true);
    statusFilter.setAutoExpand(MultiSelectComboBox.AutoExpandMode.BOTH);
    statusFilter.addValueChangeListener(e -> refreshGrid());

    HorizontalLayout filters =
        new HorizontalLayout(searchField, priorityFilter, statusFilter, quantity);
    filters.setAlignItems(Alignment.END);
    filters.setWidthFull();
    filters.setSpacing(true);

    Button newEntryButton = new Button("Nueva Entrada", e -> openForm());
    newEntryButton.addThemeVariants(
        ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);

    HorizontalLayout header = new HorizontalLayout(titleWithInfo, newEntryButton);
    header.setWidthFull();
    header.setJustifyContentMode(JustifyContentMode.BETWEEN);
    header.setAlignItems(Alignment.CENTER);

    configureGrid();
    form = new WaitingRoomForm(waitingRoomService, clientService, petService);
    form.setOnSave(dto -> refreshGrid());
    form.addDialogCloseActionListener(e -> refreshGrid());

    // add(header, grid);
    add(header, filters, grid, cardContainer);
    refreshGrid();
  }

  private void configureGrid() {
    grid.setSizeFull();

    grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    grid.addColumn(wr -> wr.getClient().getFirstName() + " " + wr.getClient().getLastName())
        .setHeader("Cliente");

    grid.addColumn(wr -> wr.getPet().getName()).setHeader("Mascota");

    grid.addColumn(WaitingRoom::getReasonForVisit).setHeader("Razón de Visita");

    grid.addComponentColumn(this::renderPriority).setHeader("Prioridad");

    grid.addColumn(
            wr -> wr.getArrivalTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
        .setHeader("Hora de Llegada");

    grid.addComponentColumn(
            wr -> {
              Employee vet = wr.getAssignedVeterinarian();
              Span badge;

              if (vet != null) {
                badge = new Span(vet.getFirstName() + " " + vet.getLastName());
                badge.getElement().getThemeList().add("badge success");
                // Verde cuando tiene veterinario asignado
              } else {
                badge = new Span("Sin asignar");
                badge.getElement().getThemeList().add("badge error");
                // Rojo cuando no tiene
              }

              return badge;
            })
        .setHeader("Veterinario");

    grid.addComponentColumn(this::renderStatus).setHeader("Estado");

    grid.asSingleSelect()
        .addValueChangeListener(
            event -> {
              WaitingRoom selected = event.getValue();
              if (selected != null) {
                openDetailsDialog(selected);
              }
            });

    grid.addComponentColumn(this::createActionsColumn).setHeader("Acciones").setAutoWidth(true);
  }

  private void openForm() {
    form.openForNew();
  }

  private void refreshGrid() {
    Specification<WaitingRoom> spec = createFilterSpecification();
    List<WaitingRoom> filtered = waitingRoomService.getRepository().findAll(spec);
    grid.setItems(filtered);
  }

  private Component renderPriority(WaitingRoom wr) {
    Span badge = new Span(wr.getPriority().name());
    badge.getElement().getThemeList().add("badge pill");

    switch (wr.getPriority()) {
      case URGENTE -> badge.getElement().getThemeList().add("error");
      case EMERGENCIA -> badge.getElement().getThemeList().add("warning");
      case NORMAL -> badge.getElement().getThemeList().add("success");
    }

    return badge;
  }

  private Component renderStatus(WaitingRoom wr) {
    if (wr.getStatus() == null) {
      return new Span("-");
    }

    Span badge = new Span(wr.getStatus().name().replace("_", " "));
    badge.getElement().getThemeList().add("badge pill");

    switch (wr.getStatus()) {
      case ESPERANDO -> badge.getElement().getThemeList().add("primary");
      case EN_CONSULTA -> badge.getElement().getThemeList().add("success");
      case COMPLETADO -> badge.getElement().getThemeList().add("contrast");
      case CANCELADO -> badge.getElement().getThemeList().add("error");
      default -> badge.getElement().getThemeList().add("badge");
    }

    return badge;
  }

  private void openDetailsDialog(WaitingRoom wr) {
    Dialog dialog = new Dialog();
    dialog.setHeaderTitle("Detalles de la Consulta");
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

    // Encabezado: Prioridad y Estado
    Span priority = new Span(wr.getPriority().name());
    priority.getElement().getThemeList().add("badge pill");
    switch (wr.getPriority()) {
      case URGENTE -> priority.getElement().getThemeList().add("error");
      case EMERGENCIA -> priority.getElement().getThemeList().add("warning");
      case NORMAL -> priority.getElement().getThemeList().add("success");
    }

    Span status = new Span(wr.getStatus().name().replace("_", " "));
    status.getElement().getThemeList().add("badge pill");
    switch (wr.getStatus()) {
      case EN_CONSULTA -> status.getElement().getThemeList().add("success");
      case COMPLETADO -> status.getElement().getThemeList().add("contrast");
      case CANCELADO -> status.getElement().getThemeList().add("error");
      default -> status.getElement().getThemeList().add("primary");
    }

    HorizontalLayout header = new HorizontalLayout(priority, status);
    header.setWidthFull();
    header.setJustifyContentMode(JustifyContentMode.BETWEEN);

    // Datos del cliente
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

    // Datos del animal
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

    Icon motivoIcon = VaadinIcon.CLIPBOARD_TEXT.create();
    motivoIcon.setColor("var(--lumo-secondary-text-color)");

    Duration waitTime = Duration.ZERO;
    if (wr.getArrivalTime() != null && !wr.getArrivalTime().isAfter(LocalDateTime.now())) {
      waitTime = Duration.between(wr.getArrivalTime(), LocalDateTime.now());
    }
    String formattedWait = String.format("%dh %02dm", waitTime.toHours(), waitTime.toMinutesPart());

    Icon waitIcon = VaadinIcon.HOURGLASS.create();
    waitIcon.setColor("var(--lumo-secondary-text-color)");
    Span waitingText = new Span("Esperando: " + formattedWait);
    HorizontalLayout waiting = new HorizontalLayout(waitIcon, waitingText);

    // Botones según el estado
    Button completeButton =
        new Button(
            "Finalizar Consulta",
            e -> {
              wr.setStatus(WaitingRoomStatus.COMPLETADO);
              waitingRoomService.update(wr);
              refreshGrid();
              dialog.close();
            });
    completeButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
    completeButton.setWidth("220px");

    /* Button startButton =
    new Button(
        "Iniciar Consulta",
        e -> {
          wr.setStatus(WaitingRoomStatus.EN_CONSULTA);
          waitingRoomService.update(wr);
          refreshGrid();
          dialog.close();
        });*/
    // startButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    Button cancelConsultation = new Button("Cancelar Consulta");
    cancelConsultation.addThemeVariants(ButtonVariant.LUMO_ERROR);

    // HorizontalLayout actions = new HorizontalLayout(startButton, cancelConsultation);
    HorizontalLayout actions = new HorizontalLayout(cancelConsultation);
    actions.setSpacing(true);
    actions.setJustifyContentMode(JustifyContentMode.CENTER);
    actions.setWidthFull();

    actions.getStyle().set("margin-top", "1rem");

    // startButton.setMinWidth("200px");
    cancelConsultation.setMinWidth("200px");

    cancelConsultation.addClickListener(
        e -> {
          ConfirmDialog dialogConfirm = new ConfirmDialog();
          dialogConfirm.setHeader("¿Estás seguro?");
          dialogConfirm.setText("Esto cancelará la consulta y marcará al paciente como cancelado.");

          dialogConfirm.setCancelable(true);
          dialogConfirm.setCancelText("No");
          dialogConfirm.setConfirmText("Sí, cancelar");

          dialogConfirm.addConfirmListener(
              event -> {
                if (wr.getStatus().equals(WaitingRoomStatus.EN_CONSULTA)) {
                  NotificationUtils.error("No se puede cancelar una entrada que está en consulta.");
                  return;
                }
                waitingRoomService.delete(wr.getId());
                refreshGrid();
                dialog.close();
              });

          dialogConfirm.open();
        });

    cancelConsultation.addThemeVariants(ButtonVariant.LUMO_ERROR);

    HorizontalLayout buttons = new HorizontalLayout();
    if (wr.getStatus() == WaitingRoomStatus.ESPERANDO) {
      // buttons.add(startButton, cancelConsultation);
      buttons.add(cancelConsultation);
    } else if (wr.getStatus() == WaitingRoomStatus.EN_CONSULTA) {
      // buttons.add(completeButton, cancelConsultation);
    }

    layout.add(header, clientName, contactInfo, petInfo, reason, notes, arrival, waiting, buttons);
    dialog.add(layout);
    dialog.open();
  }

  public Specification<WaitingRoom> createFilterSpecification() {
    return (root, query, builder) -> {
      var clientJoin = root.join("client");
      var petJoin = root.join("pet");

      String searchTerm = searchField.getValue();
      Predicate searchPredicate = builder.conjunction();

      if (searchTerm != null && !searchTerm.trim().isEmpty()) {
        String likeSearch = "%" + searchTerm.toLowerCase() + "%";

        searchPredicate =
            builder.or(
                builder.like(builder.lower(clientJoin.get("firstName")), likeSearch),
                builder.like(builder.lower(clientJoin.get("lastName")), likeSearch),
                builder.like(builder.lower(petJoin.get("name")), likeSearch));
      }

      Predicate priorityPredicate =
          createPredicateForSelectedItems(
              Optional.ofNullable(priorityFilter.getSelectedItems()),
              items -> root.get("priority").in(items),
              builder);

      Predicate statusPredicate =
          createPredicateForSelectedItems(
              Optional.ofNullable(statusFilter.getSelectedItems()),
              items -> root.get("status").in(items),
              builder);

      return builder.and(searchPredicate, priorityPredicate, statusPredicate);
    };
  }

  private Component createActionsColumn(WaitingRoom waitingRoom) {

    Button assign = new Button(new Icon(VaadinIcon.STETHOSCOPE));
    assign.addThemeVariants(
        ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
    assign.getElement().setProperty("title", "Asignar veterinario");
    assign.getStyle().set("min-width", "32px").set("width", "32px").set("padding", "0");
    assign.addClickListener(e -> openAssignDialog(waitingRoom));

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

    edit.addClickListener(e -> form.openForEdit(waitingRoom));
    delete.addClickListener(
        e -> {
          if (waitingRoom.getStatus().equals(WaitingRoomStatus.EN_CONSULTA)) {
            NotificationUtils.error("No se puede eliminar una entrada que está en consulta.");
            return;
          }
          waitingRoomService.delete(waitingRoom.getId());
          NotificationUtils.success("Espera eliminada correctamente.");
          refreshAll();
        });

    HorizontalLayout actions = new HorizontalLayout(assign, edit, delete);
    actions.setSpacing(true);
    actions.setPadding(false);
    actions.setMargin(false);
    actions.setWidth(null);
    return actions;
  }

  public void refreshAll() {
    form.close();
    refreshGrid();
    updateQuantity();
  }

  private void updateQuantity() {
    try {
      long count =
          waitingRoomService.getWaitingCount() + waitingRoomService.getInConsultationCount();
      quantity.setText("En sala de espera (" + count + ")");
    } catch (Exception e) {
      log.warn("Error getting employee count", e);
      quantity.setText("En sala de espera:");
    }
  }

  private void openAssignDialog(WaitingRoom wr) {
    Dialog d = new Dialog();
    d.setHeaderTitle("Asignar veterinario");

    ComboBox<Employee> vets = new ComboBox<>("Veterinario disponible");
    vets.setItemLabelGenerator(v -> v.getFirstName() + " " + v.getLastName());
    vets.setWidthFull();
    vets.setItems(employeeService.getAvailableVets()); // SOLO disponibles

    // Si ya hay un vet asignado, mostrarlo como valor inicial
    if (wr.getAssignedVeterinarian() != null) {
      vets.setValue(wr.getAssignedVeterinarian());
    }

    Button assign =
        new Button(
            "Asignar",
            ev -> {
              Employee selected = vets.getValue();
              if (selected == null) {
                NotificationUtils.error("Selecciona un veterinario");
                return;
              }
              try {
                // Nueva lógica: solo asignar vet (no crear consulta todavía)
                consultationService.assignFromWaitingRoom(wr.getId(), selected.getId());

                if (wr.getAssignedVeterinarian() == null) {
                  NotificationUtils.success("Veterinario asignado correctamente");
                } else {
                  NotificationUtils.success("Veterinario actualizado correctamente");
                }
                d.close();
                refreshGrid();
              } catch (Exception ex) {
                NotificationUtils.error("No se pudo asignar: " + ex.getMessage());
              }
            });
    assign.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    Button cancel = new Button("Cancelar", e -> d.close());
    cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

    VerticalLayout content = new VerticalLayout(vets);
    content.setPadding(false);

    d.add(content);
    d.getFooter().add(cancel, assign);
    d.setDraggable(true);
    d.setResizable(true);
    d.open();
  }
}
