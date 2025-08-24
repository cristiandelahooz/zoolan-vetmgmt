package com.wornux.views.calendar;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.dto.request.AppointmentUpdateRequestDto;
import com.wornux.services.interfaces.AppointmentService;
import com.wornux.services.interfaces.PetService;
import com.wornux.views.MainLayout;
import com.wornux.views.calendar.utils.FullCalendarWithTooltip;
import elemental.json.Json;
import elemental.json.JsonObject;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Route(value = "calendar", layout = MainLayout.class)
@PageTitle("Calendario de Citas")
@PermitAll
public class AppointmentCalendarView extends VerticalLayout {

  private final FullCalendar calendar;
  private final AppointmentEntryService appointmentEntryService;
  private final AppointmentService appointmentService;
  private final PetService petService;
  private final AppointmentDialog appointmentDialog;

  @Autowired
  public AppointmentCalendarView(
      AppointmentEntryService appointmentEntryService,
      AppointmentService appointmentService,
      PetService petService) {
    this.appointmentEntryService = appointmentEntryService;
    this.appointmentService = appointmentService;
    this.petService = petService;

    appointmentDialog =
        new AppointmentDialog(appointmentService, petService, (v) -> loadAppointments());

    setSizeFull();
    setPadding(true);
    setSpacing(true);

    calendar =
        FullCalendarBuilder.create()
            .withCustomType(FullCalendarWithTooltip.class)
            .withAutoBrowserTimezone()
            .withAutoBrowserLocale()
            .withInitialOptions(createDefaultInitialOptions())
            .withEntryLimit(3)
            .build();

    calendar.addThemeVariants(FullCalendarVariant.LUMO);
    calendar.setFirstDay(DayOfWeek.MONDAY);
    calendar.setNowIndicatorShown(true);
    calendar.setNumberClickable(true);
    calendar.setTimeslotsSelectable(true);

    calendar.setSlotMinTime(LocalTime.of(8, 0));
    calendar.setSlotMaxTime(LocalTime.of(20, 0));
    calendar.setBusinessHours(
        new BusinessHours(
            LocalTime.of(8, 0), LocalTime.of(20, 0), BusinessHours.DEFAULT_BUSINESS_WEEK),
        new BusinessHours(LocalTime.of(9, 0), LocalTime.of(14, 0), DayOfWeek.SATURDAY));

    calendar.addEntryClickedListener(this::onEntryClick);
    calendar.addTimeslotsSelectedListener(this::onTimeslotsSelected);
    calendar.addEntryResizedListener(this::onEntryResized);
    calendar.addEntryDroppedListener(this::onEntryDropped);

    setFlexGrow(1, calendar);
    setHorizontalComponentAlignment(Alignment.STRETCH, calendar);

    HorizontalLayout toolbar = createToolbar();

    loadAppointments();
    add(toolbar, calendar);
  }

  private HorizontalLayout createToolbar() {
    // Navigation buttons
    Button prevBtn = new Button(VaadinIcon.ANGLE_LEFT.create(), e -> calendar.previous());
    prevBtn.setTooltipText("Anterior");

    Button nextBtn = new Button(VaadinIcon.ANGLE_RIGHT.create(), e -> calendar.next());
    nextBtn.setTooltipText("Siguiente");

    Button todayBtn = new Button("Hoy", e -> calendar.today());
    todayBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

    // View selector
    ComboBox<CalendarViewImpl> viewSelector = new ComboBox<>();
    viewSelector.setItems(
        CalendarViewImpl.DAY_GRID_MONTH,
        CalendarViewImpl.TIME_GRID_WEEK,
        CalendarViewImpl.TIME_GRID_DAY,
        CalendarViewImpl.LIST_WEEK);
    viewSelector.setValue(CalendarViewImpl.DAY_GRID_MONTH);
    viewSelector.setItemLabelGenerator(this::getViewDisplayName);
    viewSelector.addValueChangeListener(
        e -> {
          if (e.getValue() != null) {
            calendar.changeView(e.getValue());
          }
        });
    viewSelector.setWidth("150px");

    // Action buttons
    Button newAppointmentBtn =
        new Button("Nueva Cita", VaadinIcon.PLUS.create(), e -> openNewAppointmentDialog());
    newAppointmentBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    Button refreshBtn = new Button(VaadinIcon.REFRESH.create(), e -> loadAppointments());
    refreshBtn.setTooltipText("Actualizar");

    // Layout
    HorizontalLayout navGroup = new HorizontalLayout(prevBtn, nextBtn, todayBtn);
    navGroup.setSpacing(false);
    navGroup.addClassName(LumoUtility.Gap.XSMALL);

    HorizontalLayout actionGroup = new HorizontalLayout(newAppointmentBtn, refreshBtn);
    actionGroup.addClassName(LumoUtility.Gap.SMALL);

    HorizontalLayout toolbar = new HorizontalLayout();
    toolbar.setWidthFull();
    toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);
    toolbar.add(navGroup, viewSelector, actionGroup);
    toolbar.setAlignItems(Alignment.CENTER);
    toolbar.addClassName(LumoUtility.Gap.MEDIUM);
    toolbar.addClassName(LumoUtility.Padding.MEDIUM);

    return toolbar;
  }

  private String getViewDisplayName(CalendarViewImpl view) {
    return switch (view) {
      case DAY_GRID_MONTH -> "Mes";
      case TIME_GRID_WEEK -> "Semana";
      case TIME_GRID_DAY -> "Día";
      case LIST_WEEK -> "Lista Semanal";
      default -> view.getClientSideValue();
    };
  }

  private void openNewAppointmentDialog() {
    appointmentDialog.openForNew(LocalDateTime.now(), LocalDateTime.now().plusHours(1));
  }

  public void loadAppointments() {
    try {
      List<Entry> entries = appointmentEntryService.getAppointmentEntries();
      InMemoryEntryProvider<Entry> entryProvider = EntryProvider.inMemoryFrom(entries);
      calendar.setEntryProvider(entryProvider);
      calendar.getEntryProvider().refreshAll();
    } catch (Exception e) {
      log.error("Error loading appointments", e);
    }
  }

  public void refreshAll(LocalDate localDate) {
    calendar.gotoDate(localDate);
    loadAppointments();
  }

  public void changeView(org.vaadin.stefan.fullcalendar.CalendarView selectedView) {
    calendar.changeView(selectedView);
  }

  public Optional<org.vaadin.stefan.fullcalendar.CalendarView> getCurrentView() {
    return calendar.getCurrentView();
  }

  private void onEntryClick(EntryClickedEvent event) {
    Long appointmentId = Long.valueOf(event.getEntry().getId());
    appointmentEntryService
        .getAppointment(appointmentId)
        .ifPresent(
            appointment -> {
              appointmentDialog.openForEdit(appointment);
            });
  }

  private void onTimeslotsSelected(TimeslotsSelectedEvent event) {
    LocalDateTime start = event.getStart();
    LocalDateTime end = event.getEnd();
    appointmentDialog.openForNew(start, end);
  }

  private void onEntryResized(EntryResizedEvent event) {
    Entry entry = event.getEntry();
    event.applyChangesOnEntry();
    updateAppointmentTime(entry);
  }

  private void onEntryDropped(EntryDroppedEvent event) {
    Entry entry = event.getEntry();
    event.applyChangesOnEntry();
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
      loadAppointments(); // Reload to revert visual changes
    }
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
    initialOptions.put(
        "eventTimeFormat",
        Json.parse("{\"hour\": \"2-digit\", \"minute\": \"2-digit\", \"meridiem\": false}"));

    return initialOptions;
  }
}
