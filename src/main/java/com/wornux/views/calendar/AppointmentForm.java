package com.wornux.views.calendar;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.components.Sidebar;
import com.wornux.data.entity.AppointmentClientInfo;
import com.wornux.data.entity.Client;
import com.wornux.data.entity.Pet;
import com.wornux.data.enums.OfferingType;
import com.wornux.data.enums.PetType;
import com.wornux.dto.request.AppointmentCreateRequestDto;
import com.wornux.dto.request.AppointmentUpdateRequestDto;
import com.wornux.dto.response.AppointmentResponseDto;
import com.wornux.mapper.ClientMapper;
import com.wornux.services.interfaces.AppointmentService;
import com.wornux.services.interfaces.ClientService;
import com.wornux.services.interfaces.PetService;
import com.wornux.utils.CommonUtils;
import com.wornux.utils.MenuBarHandler;
import com.wornux.utils.ValidationNotificationUtils;
import com.wornux.views.customers.ClientCreationDialog;
import com.wornux.views.pets.PetForm;
import jakarta.validation.ConstraintViolationException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static com.wornux.utils.CSSUtility.CARD_BACKGROUND_COLOR;
import static com.wornux.utils.CSSUtility.SLIDER_RESPONSIVE_WIDTH;
import static com.wornux.utils.CommonUtils.comboBoxItemFilter;
import static com.wornux.utils.CommonUtils.createIconItem;

@Slf4j
public class AppointmentForm extends Div {

  private final transient AppointmentService appointmentService;
  private final transient ClientService clientService;
  private final transient PetService petService;
  private final Consumer<Void> onSaveCallback;
  private final Select<OfferingType> offeringTypeSelect = new Select<>();
  private final ComboBox<Client> clientCombo = new ComboBox<>("Selecciona un cliente");
  private final ComboBox<Pet> petCombo = new ComboBox<>("Selecciona una mascota");
  private final TextField titleField = new TextField("Título de la Cita");
  private final DatePicker appointmentDate = new DatePicker("Fecha de la Cita");
  private final TimePicker startTime = new TimePicker("Hora de Inicio");
  private final TimePicker endTime = new TimePicker("Hora de Fin");
  private final TextArea notesField = new TextArea("Notas");
  // AppointmentClientInfo fields for grooming workflow
  private final TextField guestClientName = new TextField("Nombre del Cliente");
  private final TextField guestClientPhone = new TextField("Teléfono");
  private final TextField guestClientEmail = new TextField("Email");
  private final Select<PetType> guestPetType = new Select<>();
  private final TextField guestPetBreed = new TextField("Raza de la Mascota");
  private final Binder<AppointmentCreateRequestDto> binder =
      new BeanValidationBinder<>(AppointmentCreateRequestDto.class);
  private final Sidebar sidebar = new Sidebar();
  private final Button addClient = new Button(VaadinIcon.PLUS_CIRCLE.create());
  private final Button addPet = new Button(VaadinIcon.PLUS_CIRCLE.create());
  private final Div layoutTabBar = new Div();
  private final Div generalFormDiv = new Div();
  private final ClientCreationDialog clientCreationDialog;
  private final PetForm petForm;
  private transient AppointmentResponseDto currentAppointment;
  private boolean isGroomingWorkflow = false;

  @Setter private transient Runnable callable;

  public AppointmentForm(
      AppointmentService appointmentService,
      ClientService clientService,
      PetService petService,
      ClientMapper clientMapper,
      Consumer<Void> onSaveCallback) {
    this.appointmentService = appointmentService;
    this.clientService = clientService;
    this.petService = petService;
    this.onSaveCallback = onSaveCallback;

    this.clientCreationDialog = new ClientCreationDialog(clientService, clientMapper);
    this.petForm = new PetForm(petService, clientService);

    initializeForm();
    setupSidebar();
    setupBinder();

    add(sidebar, clientCreationDialog, petForm);
  }

  private void initializeForm() {
    offeringTypeSelect.setLabel("Tipo de Servicio");
    offeringTypeSelect.setItems(OfferingType.values());
    offeringTypeSelect.setItemLabelGenerator(OfferingType::getDisplay);
    offeringTypeSelect.setRequiredIndicatorVisible(true);
    offeringTypeSelect.setWidthFull();
    offeringTypeSelect.addValueChangeListener(
        event -> {
          isGroomingWorkflow = event.getValue() == OfferingType.GROOMING;
          enableFormFields(event.getValue() != null);
          updateWorkflowVisibility();
        });

    clientCombo.setClearButtonVisible(true);
    clientCombo.setWidthFull();
    clientCombo.setRequiredIndicatorVisible(true);
    clientCombo.setEnabled(false);
    setupClientCombo();

    petCombo.setClearButtonVisible(true);
    petCombo.setWidthFull();
    petCombo.setRequiredIndicatorVisible(true);
    petCombo.setEnabled(false);

    titleField.setRequired(true);
    titleField.setWidthFull();
    titleField.setEnabled(false);

    appointmentDate.setRequired(true);
    appointmentDate.setWidthFull();
    appointmentDate.setEnabled(false);

    startTime.setRequired(true);
    startTime.setWidth("48%");
    startTime.setEnabled(false);
    startTime.addValueChangeListener(
        event -> {
          if (event.getValue() != null && endTime.isEmpty()) {
            endTime.setValue(event.getValue().plusHours(1));
          }
        });

    endTime.setRequired(true);
    endTime.setWidth("48%");
    endTime.setEnabled(false);

    notesField.setWidthFull();
    notesField.setEnabled(false);
    CommonUtils.commentsFormat(notesField, 500);

    initializeGuestClientFields();

    addClient.setTooltipText("Agregar un cliente");
    addClient.addThemeVariants(
        ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
    addClient.addClassNames(LumoUtility.Width.AUTO);
    addClient.setEnabled(false);
    addClient.addClickListener(this::openClientCreationDialog);

    addPet.setTooltipText("Agregar una mascota");
    addPet.addThemeVariants(
        ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
    addPet.addClassNames(LumoUtility.Width.AUTO);
    addPet.setEnabled(false);
    addPet.addClickListener(this::openPetCreationDialog);
  }

  private void initializeGuestClientFields() {
    guestClientName.setRequired(true);
    guestClientName.setWidthFull();

    guestClientPhone.setRequired(true);
    guestClientPhone.setWidthFull();

    guestClientEmail.setRequired(true);
    guestClientEmail.setWidthFull();

    guestPetType.setLabel("Tipo de Mascota");
    guestPetType.setItems(PetType.values());
    guestPetType.setItemLabelGenerator(PetType::name);
    guestPetType.setRequiredIndicatorVisible(true);
    guestPetType.setWidthFull();

    guestPetBreed.setRequired(true);
    guestPetBreed.setWidthFull();

    Arrays.asList(guestClientName, guestClientPhone, guestClientEmail, guestPetType, guestPetBreed)
        .forEach(field -> field.setVisible(false));
  }

  private void setupSidebar() {
    layoutTabBar.addClassNames(CARD_BACKGROUND_COLOR, LumoUtility.Padding.SMALL);
    layoutTabBar.addClassNames(
        LumoUtility.Display.FLEX,
        LumoUtility.FlexDirection.COLUMN,
        LumoUtility.Width.AUTO,
        LumoUtility.Height.FULL);

    generalFormDiv.add(createGeneralForm());

    sidebar.createContent(layoutTabBar, generalFormDiv);
    sidebar.addClassNames(SLIDER_RESPONSIVE_WIDTH);
    sidebar.addSubTitle("Completa el formulario para crear una cita.");

    sidebar.setOnSaveClickListener(this::saveOrUpdate);
    sidebar.setOnCancelClickListener(this::cancel);

    sidebar.getSave().setText("Guardar Cita");
  }

  private void setupBinder() {
    binder
        .forField(titleField)
        .bind(AppointmentCreateRequestDto::getReason, AppointmentCreateRequestDto::setReason);

    binder
        .forField(notesField)
        .bind(AppointmentCreateRequestDto::getNotes, AppointmentCreateRequestDto::setNotes);

    binder
        .forField(offeringTypeSelect)
        .asRequired("El tipo de servicio es obligatorio")
        .bind(
            AppointmentCreateRequestDto::getOfferingType,
            AppointmentCreateRequestDto::setOfferingType);

    titleField.setClearButtonVisible(true);
    notesField.setClearButtonVisible(true);
    guestClientName.setClearButtonVisible(true);
    guestClientPhone.setClearButtonVisible(true);
    guestClientEmail.setClearButtonVisible(true);
    guestPetBreed.setClearButtonVisible(true);
  }

  private void setupClientCombo() {
    List<Client> allActiveClients = clientService.getAllActiveClients();
    clientCombo.setItems(
        comboBoxItemFilter(Client::getFirstName, String::contains), allActiveClients);
    clientCombo.setItemLabelGenerator(Client::getFirstName);
    clientCombo.setRenderer(
        new ComponentRenderer<>(
            item -> {
              Div container = new Div();
              container.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

              Span title = new Span(item.getFirstName());
              title.addClassNames(LumoUtility.FontWeight.BOLD);

              Span subtitle = new Span(item.getFullName());
              subtitle.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

              container.add(title, subtitle);
              return container;
            }));
    clientCombo.addValueChangeListener(
        event -> {
          Client selectedClient = event.getValue();
          if (selectedClient != null) {
            loadClientPets(selectedClient);
            petCombo.setEnabled(true);
            addPet.setEnabled(true);
          } else {
            petCombo.clear();
            petCombo.setEnabled(false);
            addPet.setEnabled(false);
          }
        });
  }

  private void loadClientPets(Client client) {
    try {
      List<Pet> clientPets =
          petService.getPetsByOwnerId(client.getId(), PageRequest.of(0, 1000)).stream()
              .map(
                  dto -> {
                    Pet pet = new Pet();
                    pet.setId(dto.getId());
                    pet.setName(dto.getName());
                    return pet;
                  })
              .toList();

      petCombo.setItems(clientPets);
      petCombo.setItemLabelGenerator(Pet::getName);
    } catch (Exception e) {
      log.error("Error loading client pets", e);
      Notification.show("Error cargando mascotas del cliente", 3000, Notification.Position.MIDDLE)
          .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
  }

  private void openClientCreationDialog(ClickEvent<Button> event) {
    clientCreationDialog.setOnClientCreated(
        item -> {
          clientCombo.setValue(item);
          clientCombo.getDataProvider().refreshAll();
        });
    clientCreationDialog.openDialog();
  }

  private void openPetCreationDialog(ClickEvent<Button> event) {
    if (clientCombo.getValue() == null) {
      Notification.show(
              "Por favor selecciona un cliente primero", 3000, Notification.Position.MIDDLE)
          .addThemeVariants(NotificationVariant.LUMO_ERROR);
      return;
    }

    petForm.setOnSaveCallback(() -> loadClientPets(clientCombo.getValue()));

    petForm.openForNewWithOwner(clientCombo.getValue());
  }

  private void enableFormFields(boolean enabled) {
    titleField.setEnabled(enabled);
    appointmentDate.setEnabled(enabled);
    startTime.setEnabled(enabled);
    endTime.setEnabled(enabled);
    notesField.setEnabled(enabled);

    clientCombo.setEnabled(enabled && !isGroomingWorkflow);
    addClient.setEnabled(enabled && !isGroomingWorkflow);
    addPet.setEnabled(enabled && !isGroomingWorkflow && clientCombo.getValue() != null);

    if (enabled && isGroomingWorkflow) {
      guestClientName.setEnabled(true);
      guestClientPhone.setEnabled(true);
      guestClientEmail.setEnabled(true);
      guestPetType.setEnabled(true);
      guestPetBreed.setEnabled(true);
    } else {
      guestClientName.setEnabled(false);
      guestClientPhone.setEnabled(false);
      guestClientEmail.setEnabled(false);
      guestPetType.setEnabled(false);
      guestPetBreed.setEnabled(false);
    }
  }

  private void updateWorkflowVisibility() {
    boolean showGuestFields = isGroomingWorkflow;
    boolean showClientPetSelection = !isGroomingWorkflow;

    Arrays.asList(guestClientName, guestClientPhone, guestClientEmail, guestPetType, guestPetBreed)
        .forEach(field -> field.setVisible(showGuestFields));

    clientCombo.setVisible(showClientPetSelection);
    petCombo.setVisible(showClientPetSelection);
    addClient.setVisible(showClientPetSelection);
    addPet.setVisible(showClientPetSelection);
  }

  private Div createTabBar() {
    MenuBar menuBar = new MenuBar();
    menuBar.addThemeVariants(MenuBarVariant.LUMO_ICON);

    MenuItem general =
        createIconItem(menuBar, VaadinIcon.INFO_CIRCLE.create(), "Información general");

    MenuBarHandler menuBarHandler = new MenuBarHandler(menuBar, layoutTabBar);
    menuBarHandler.addMenuItem(general, generalFormDiv);

    if (currentAppointment != null) {
      MenuItem activityLog =
          createIconItem(menuBar, VaadinIcon.TIME_BACKWARD.create(), "Registro de actividad");
      menuBarHandler.addMenuItem(activityLog, createActivityLogForm());
    }

    menuBarHandler.setDefaultMenuItem(general);

    Div tabs = new Div(menuBar);
    tabs.addClassNames(
        LumoUtility.Padding.SMALL,
        LumoUtility.Gap.MEDIUM,
        LumoUtility.Background.CONTRAST_10,
        LumoUtility.Margin.Horizontal.MEDIUM,
        LumoUtility.Margin.Top.MEDIUM);
    tabs.getStyle().set("border-top-left-radius", "var(--lumo-space-m)");
    tabs.getStyle().set("border-top-right-radius", "var(--lumo-space-m)");

    return tabs;
  }

  private Div createGeneralForm() {
    Div clientSection = createClientSection();
    Div appointmentDetailsSection = createAppointmentDetailsSection();
    Div guestInfoSection = createGuestInfoSection();

    Div form =
        new Div(
            offeringTypeSelect,
            clientSection,
            appointmentDetailsSection,
            guestInfoSection,
            notesField);
    form.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);
    form.addClassNames(LumoUtility.Gap.SMALL, LumoUtility.Padding.SMALL);

    return form;
  }

  private Div createClientSection() {
    Div clientHeader = new Div(clientCombo, addClient);
    clientHeader.addClassNames(
        LumoUtility.Display.FLEX, LumoUtility.FlexDirection.ROW, LumoUtility.AlignItems.END);

    Div petHeader = new Div(petCombo, addPet);
    petHeader.addClassNames(
        LumoUtility.Display.FLEX, LumoUtility.FlexDirection.ROW, LumoUtility.AlignItems.END);

    Div section = new Div(clientHeader, petHeader);
    section.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);
    section.addClassNames(LumoUtility.Gap.SMALL);

    return section;
  }

  private Div createAppointmentDetailsSection() {
    Div timeLabelDiv = new Div(new Span("Horario de la Cita"));
    timeLabelDiv.addClassNames(
        LumoUtility.FontSize.SMALL, LumoUtility.FontWeight.MEDIUM, LumoUtility.TextColor.SECONDARY);

    Div timeLayout = new Div(startTime, endTime);
    timeLayout.addClassNames(
        LumoUtility.Display.FLEX,
        LumoUtility.FlexDirection.ROW,
        LumoUtility.Gap.MEDIUM,
        LumoUtility.JustifyContent.BETWEEN);

    Div timeSection = new Div(timeLabelDiv, timeLayout);
    timeSection.addClassNames(
        LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Gap.SMALL);

    Div section = new Div(titleField, appointmentDate, timeSection);
    section.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);
    section.addClassNames(LumoUtility.Gap.SMALL);

    return section;
  }

  private Div createGuestInfoSection() {
    Div section =
        new Div(guestClientName, guestClientPhone, guestClientEmail, guestPetType, guestPetBreed);
    section.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);
    section.addClassNames(LumoUtility.Gap.SMALL);

    return section;
  }

  private Div createActivityLogForm() {
    H1 headerTitle = new H1("Registro de actividad");

    Paragraph description =
        new Paragraph(
            "Registro cronológico de todas las acciones realizadas en esta cita. Incluye cambios de"
                + " estado, interacciones de usuario, notas y cualquier modificación para"
                + " trazabilidad completa.");
    description.addClassNames(
        LumoUtility.Display.HIDDEN, LumoUtility.Display.Breakpoint.Large.FLEX);

    Div headerLayout = new Div(headerTitle, description);
    headerLayout.addClassNames(
        LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Margin.Top.SMALL);

    return headerLayout;
  }

  private void cancel(ClickEvent<Button> buttonClickEvent) {
    sidebar.close();
  }

  private void saveOrUpdate(ClickEvent<Button> buttonClickEvent) {
    if (!validateForm()) {
      return;
    }

    try {
      if (currentAppointment == null) {
        createNewAppointment();
      } else {
        updateExistingAppointment();
      }

      onSaveCallback.accept(null);
      sidebar.close();

    } catch (ConstraintViolationException e) {
      log.error("Validation error saving appointment", e);
      ValidationNotificationUtils.handleFormValidationErrors(e);
    } catch (ObjectOptimisticLockingFailureException ex) {
      log.error(ex.getLocalizedMessage());
      Notification.show(
              "Error al actualizar los datos. Alguien más ha actualizado el registro mientras"
                  + " realizabas cambios.",
              3000,
              Notification.Position.MIDDLE)
          .addThemeVariants(NotificationVariant.LUMO_ERROR);
    } catch (Exception e) {
      log.error("Error saving appointment", e);
      Notification.show("Error guardando la cita", 3000, Notification.Position.MIDDLE)
          .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
  }

  private boolean validateForm() {
    if (offeringTypeSelect.isEmpty()) {
      Notification.show("Por favor selecciona un tipo de servicio");
      return false;
    }

    if (titleField.isEmpty()
        || appointmentDate.isEmpty()
        || startTime.isEmpty()
        || endTime.isEmpty()) {
      Notification.show("Por favor complete todos los campos requeridos");
      return false;
    }

    if (startTime.getValue() != null
        && endTime.getValue() != null
        && !startTime.getValue().isBefore(endTime.getValue())) {
      Notification.show("La hora de inicio debe ser anterior a la hora de fin");
      return false;
    }

    if (isGroomingWorkflow) {
      if (guestClientName.isEmpty()
          || guestClientPhone.isEmpty()
          || guestClientEmail.isEmpty()
          || guestPetType.isEmpty()
          || guestPetBreed.isEmpty()) {
        Notification.show("Por favor complete toda la información del cliente y mascota");
        return false;
      }
    } else {
      if (clientCombo.isEmpty() || petCombo.isEmpty()) {
        Notification.show("Por favor selecciona un cliente y una mascota");
        return false;
      }
    }

    return true;
  }

  private void createNewAppointment() {
    AppointmentCreateRequestDto createDto = new AppointmentCreateRequestDto();
    populateCreateDto(createDto);

    appointmentService.createAppointment(createDto);

    Notification.show("Cita creada exitosamente", 3000, Notification.Position.BOTTOM_END)
        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
  }

  private void updateExistingAppointment() {
    AppointmentUpdateRequestDto updateDto = new AppointmentUpdateRequestDto();
    LocalDateTime startDateTime =
        LocalDateTime.of(appointmentDate.getValue(), startTime.getValue());
    LocalDateTime endDateTime = LocalDateTime.of(appointmentDate.getValue(), endTime.getValue());

    updateDto.setStartAppointmentDate(startDateTime);
    updateDto.setEndAppointmentDate(endDateTime);

    appointmentService.updateAppointment(currentAppointment.getEventId(), updateDto);

    Notification.show("Cita actualizada exitosamente", 3000, Notification.Position.BOTTOM_END)
        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
  }

  private void populateCreateDto(AppointmentCreateRequestDto dto) {
    dto.setReason(titleField.getValue());
    dto.setOfferingType(offeringTypeSelect.getValue());
    dto.setNotes(notesField.getValue());

    LocalDateTime startDateTime =
        LocalDateTime.of(appointmentDate.getValue(), startTime.getValue());
    LocalDateTime endDateTime = LocalDateTime.of(appointmentDate.getValue(), endTime.getValue());
    dto.setStartAppointmentDate(startDateTime);
    dto.setEndAppointmentDate(endDateTime);

    if (isGroomingWorkflow) {
      AppointmentClientInfo guestInfo =
          AppointmentClientInfo.builder()
              .name(guestClientName.getValue())
              .phone(guestClientPhone.getValue())
              .email(guestClientEmail.getValue())
              .petType(guestPetType.getValue())
              .breed(guestPetBreed.getValue())
              .build();
      dto.setGuestClientInfo(guestInfo);
    } else {
      if (petCombo.getValue() != null) {
        dto.setPetId(petCombo.getValue().getId());
      }
    }
  }

  private void clearForm() {
    titleField.clear();
    offeringTypeSelect.clear();
    clientCombo.clear();
    petCombo.clear();
    appointmentDate.clear();
    startTime.clear();
    endTime.clear();
    notesField.clear();

    guestClientName.clear();
    guestClientPhone.clear();
    guestClientEmail.clear();
    guestPetType.clear();
    guestPetBreed.clear();

    isGroomingWorkflow = false;
    updateWorkflowVisibility();
  }

  private void populateForm(AppointmentResponseDto appointment) {
    if (appointment != null) {
      offeringTypeSelect.setValue(appointment.getOfferingType());
      titleField.setValue(
          appointment.getAppointmentTitle() != null ? appointment.getAppointmentTitle() : "");

      if (appointment.getStartAppointmentDate() != null) {
        appointmentDate.setValue(appointment.getStartAppointmentDate().toLocalDate());
        startTime.setValue(appointment.getStartAppointmentDate().toLocalTime());
      }

      if (appointment.getEndAppointmentDate() != null) {
        endTime.setValue(appointment.getEndAppointmentDate().toLocalTime());
      }
    }
  }

  private void recreateHeaderContent() {
    sidebar.clearHeaderContent();
    sidebar.createHeaderContent(createTabBar());
  }

  public void openForNew(LocalDateTime startTime, LocalDateTime endTime) {
    currentAppointment = null;
    clearForm();

    if (startTime != null) {
      appointmentDate.setValue(startTime.toLocalDate());
      this.startTime.setValue(startTime.toLocalTime());
      if (endTime != null) {
        this.endTime.setValue(endTime.toLocalTime());
      } else {
        this.endTime.setValue(startTime.toLocalTime().plusHours(1));
      }
    }

    recreateHeaderContent();
    sidebar.getCancel().setText("Cancelar");
    sidebar.newObject("Nueva Cita");
  }

  public void openForEdit(AppointmentResponseDto appointment) {
    currentAppointment = appointment;
    populateForm(appointment);

    recreateHeaderContent();
    sidebar.getCancel().setText("Descartar cambios");
    sidebar.editObject("Editar Cita");
  }

  public void close() {
    sidebar.close();
  }
}
