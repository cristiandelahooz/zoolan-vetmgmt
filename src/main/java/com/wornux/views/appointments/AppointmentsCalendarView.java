package com.wornux.views.appointments;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.Pet;
import com.wornux.data.enums.ServiceType;
import com.wornux.dto.request.AppointmentCreateRequestDto;
import com.wornux.dto.request.AppointmentUpdateRequestDto;
import com.wornux.dto.response.AppointmentResponseDto;
import com.wornux.services.interfaces.AppointmentService;
import com.wornux.services.interfaces.PetService;
import com.wornux.views.MainLayout;
import elemental.json.Json;
import elemental.json.JsonObject;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Route(value = "appointments", layout = MainLayout.class)
@PageTitle("Citas")
@RolesAllowed({"ROLE_SYSTEM_ADMIN", "ROLE_MANAGER", "ROLE_USER"})
public class AppointmentsCalendarView extends VerticalLayout {

    private final FullCalendar calendar;
    private final AppointmentService appointmentService;
    private final PetService petService;
    private Dialog appointmentDialog;
    private Entry currentEntry;

    @Autowired
    public AppointmentsCalendarView(AppointmentService appointmentService, PetService petService) {
        this.appointmentService = appointmentService;
        this.petService = petService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Header
        H2 title = new H2("Calendario de Citas");
        title.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

        // Calendar setup
        calendar = FullCalendarBuilder.create().withAutoBrowserTimezone().withAutoBrowserLocale()
                .withInitialOptions(createDefaultInitialOptions()).withEntryLimit(3).build();

        configureCalendar();
        loadAppointments();

        // Toolbar
        HorizontalLayout toolbar = createToolbar();

        add(title, toolbar, calendar);
        setFlexGrow(1, calendar);
    }

    private void configureCalendar() {
        calendar.addThemeVariants(FullCalendarVariant.LUMO);
        calendar.setFirstDay(DayOfWeek.MONDAY);
        calendar.setNowIndicatorShown(true);
        calendar.setNumberClickable(true);
        calendar.setTimeslotsSelectable(true);

        calendar.setSlotMinTime(LocalTime.of(8, 0));
        calendar.setSlotMaxTime(LocalTime.of(20, 0));

        calendar.setBusinessHours(
                new BusinessHours(LocalTime.of(8, 0), LocalTime.of(20, 0), BusinessHours.DEFAULT_BUSINESS_WEEK),
                new BusinessHours(LocalTime.of(9, 0), LocalTime.of(14, 0), DayOfWeek.SATURDAY));

        // Event handlers
        calendar.addTimeslotsSelectedListener(this::onTimeslotSelected);
        calendar.addEntryClickedListener(this::onEntryClicked);
        // calendar.addEntryDroppedListener(this::onEntryDropped); // Commented out - needs scheduler
        calendar.addEntryResizedListener(this::onEntryResized);
        calendar.addDatesRenderedListener(event -> loadAppointments());
    }

    private JsonObject createDefaultInitialOptions() {
        JsonObject initialOptions = Json.createObject();

        JsonObject headerToolbar = Json.createObject();
        headerToolbar.put("left", "prev,next today");
        headerToolbar.put("center", "title");
        headerToolbar.put("right", "dayGridMonth,timeGridWeek,timeGridDay,listWeek");

        initialOptions.put("headerToolbar", headerToolbar);
        initialOptions.put("weekNumbers", true);
        initialOptions.put("weekNumberCalculation", "ISO");
        initialOptions.put("eventTimeFormat",
                Json.parse("{\"hour\": \"2-digit\", \"minute\": \"2-digit\", \"meridiem\": false}"));

        return initialOptions;
    }

    private HorizontalLayout createToolbar() {
        Button newAppointmentBtn = new Button("Nueva Cita", e -> openNewAppointmentDialog());
        newAppointmentBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button refreshBtn = new Button("Actualizar", e -> loadAppointments());

        Button todayBtn = new Button("Hoy", e -> calendar.today());

        HorizontalLayout toolbar = new HorizontalLayout(newAppointmentBtn, refreshBtn, todayBtn);
        toolbar.setAlignItems(Alignment.CENTER);
        toolbar.addClassName(LumoUtility.Gap.MEDIUM);

        return toolbar;
    }

    private void loadAppointments() {
        try {
            List<AppointmentResponseDto> appointments = appointmentService.getAllAppointments(PageRequest.of(0, 1000))
                    .getContent();

            List<Entry> entries = appointments.stream().map(this::convertToEntry).toList();

            InMemoryEntryProvider<Entry> entryProvider = EntryProvider.inMemoryFrom(entries);
            calendar.setEntryProvider(entryProvider);
            calendar.getEntryProvider().refreshAll();
        } catch (Exception e) {
            log.error("Error loading appointments", e);
            Notification.show("Error al cargar las citas", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private Entry convertToEntry(AppointmentResponseDto appointment) {
        Entry entry = new Entry(String.valueOf(appointment.getEventId()));
        entry.setTitle(appointment.getAppointmentTitle());
        entry.setStart(appointment.getStartAppointmentDate());
        entry.setEnd(appointment.getEndAppointmentDate());
        entry.setDescription(
                String.format("Mascota: %s | Servicio: %s", appointment.getPetName(), appointment.getServiceType()));

        // Set color based on service type
        if (appointment.getServiceType() != null) {
            entry.setColor("#2196F3"); // Default blue color for all appointments
        }

        entry.setEditable(true);
        return entry;
    }

    private void onTimeslotSelected(TimeslotsSelectedEvent event) {
        LocalDateTime start = event.getStart();
        LocalDateTime end = event.getEnd();

        openNewAppointmentDialog(start, end);
    }

    private void onEntryClicked(EntryClickedEvent event) {
        currentEntry = event.getEntry();
        openEditAppointmentDialog(currentEntry);
    }

    /*private void onEntryDropped(EntryDroppedSchedulerEvent event) {
        Entry entry = event.getEntry();
        updateAppointmentTime(entry);
    }*/

    private void onEntryResized(EntryResizedEvent event) {
        Entry entry = event.getEntry();
        updateAppointmentTime(entry);
    }

    private void updateAppointmentTime(Entry entry) {
        try {
            Long appointmentId = Long.parseLong(entry.getId());
            AppointmentUpdateRequestDto updateDto = new AppointmentUpdateRequestDto();
            updateDto.setStartAppointmentDate(entry.getStart());
            updateDto.setEndAppointmentDate(entry.getEnd());

            appointmentService.updateAppointment(appointmentId, updateDto);

            Notification.show("Cita actualizada", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            log.error("Error updating appointment", e);
            Notification.show("Error al actualizar la cita", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            loadAppointments(); // Reload to revert changes
        }
    }

    private void openNewAppointmentDialog() {
        openNewAppointmentDialog(LocalDateTime.now(), LocalDateTime.now().plusHours(1));
    }

    private void openNewAppointmentDialog(LocalDateTime start, LocalDateTime end) {
        appointmentDialog = new Dialog();
        appointmentDialog.setHeaderTitle("Nueva Cita");
        appointmentDialog.setWidth("500px");

        FormLayout formLayout = new FormLayout();

        TextField titleField = new TextField("Título");
        titleField.setRequired(true);
        titleField.setWidthFull();

        Select<Pet> petSelect = new Select<>();
        petSelect.setLabel("Mascota");
        petSelect.setRequiredIndicatorVisible(true);
        petSelect.setWidthFull();
        petSelect.setItemLabelGenerator(
                pet -> pet.getName() + " - " + (pet.getOwners().isEmpty() ? "Sin dueño" : pet.getOwners().get(0)
                                                                                                  .getFirstName() + " " + pet.getOwners()
                                                                                                  .get(0)
                                                                                                  .getLastName()));

        // Load pets
        List<Pet> pets = petService.getAllPets(PageRequest.of(0, 1000)).stream().map(dto -> {
            Pet pet = new Pet();
            pet.setId(dto.id());
            pet.setName(dto.name());
            // Note: You'll need to adapt this based on your actual Pet entity
            return pet;
        }).collect(Collectors.toList());
        petSelect.setItems(pets);

        Select<ServiceType> serviceTypeSelect = new Select<>();
        serviceTypeSelect.setLabel("Tipo de Servicio");
        serviceTypeSelect.setItems(ServiceType.values());
        serviceTypeSelect.setItemLabelGenerator(ServiceType::name);
        serviceTypeSelect.setRequiredIndicatorVisible(true);
        serviceTypeSelect.setWidthFull();

        TextField notesField = new TextField("Notas");
        notesField.setWidthFull();

        formLayout.add(titleField, petSelect, serviceTypeSelect, notesField);
        formLayout.setColspan(notesField, 2);

        Button saveButton = new Button("Guardar", e -> {
            if (titleField.isEmpty() || petSelect.isEmpty() || serviceTypeSelect.isEmpty()) {
                Notification.show("Por favor complete todos los campos requeridos");
                return;
            }

            AppointmentCreateRequestDto createDto = new AppointmentCreateRequestDto();
            createDto.setReason(titleField.getValue()); // Using reason field for title
            createDto.setPetId(petSelect.getValue().getId());
            createDto.setServiceType(serviceTypeSelect.getValue());
            createDto.setStartAppointmentDate(start);
            createDto.setEndAppointmentDate(end);
            createDto.setNotes(notesField.getValue());

            try {
                appointmentService.createAppointment(createDto);
                Notification.show("Cita creada exitosamente", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                appointmentDialog.close();
                loadAppointments();
            } catch (Exception ex) {
                log.error("Error creating appointment", ex);
                Notification.show("Error al crear la cita", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancelar", e -> appointmentDialog.close());

        appointmentDialog.add(formLayout);
        appointmentDialog.getFooter().add(cancelButton, saveButton);
        appointmentDialog.open();
    }

    private void openEditAppointmentDialog(Entry entry) {
        appointmentDialog = new Dialog();
        appointmentDialog.setHeaderTitle("Editar Cita");
        appointmentDialog.setWidth("500px");

        FormLayout formLayout = new FormLayout();

        TextField titleField = new TextField("Título");
        titleField.setValue(entry.getTitle());
        titleField.setRequired(true);
        titleField.setWidthFull();

        // Add form fields similar to create dialog
        // ... (implementation similar to openNewAppointmentDialog)

        Button saveButton = new Button("Actualizar", e -> {
            // Update logic
            appointmentDialog.close();
            loadAppointments();
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button deleteButton = new Button("Eliminar", e -> {
            // Confirm and delete
            try {
                appointmentService.deleteAppointment(Long.parseLong(entry.getId()));
                Notification.show("Cita eliminada", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                appointmentDialog.close();
                loadAppointments();
            } catch (Exception ex) {
                log.error("Error deleting appointment", ex);
                Notification.show("Error al eliminar la cita", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Cancelar", e -> appointmentDialog.close());

        appointmentDialog.add(formLayout);
        appointmentDialog.getFooter().add(deleteButton, cancelButton, saveButton);
        appointmentDialog.open();
    }
}
