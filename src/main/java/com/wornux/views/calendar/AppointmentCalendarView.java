package com.wornux.views.calendar;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.dto.request.AppointmentUpdateRequestDto;
import com.wornux.mapper.ClientMapper;
import com.wornux.services.AuditService;
import com.wornux.services.interfaces.AppointmentService;
import com.wornux.services.interfaces.ClientService;
import com.wornux.services.interfaces.PetService;
import com.wornux.utils.ValidationNotificationUtils;
import com.wornux.views.MainLayout;
import com.wornux.views.calendar.utils.FullCalendarWithTooltip;
import elemental.json.Json;
import elemental.json.JsonObject;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.CallbackEntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Route(value = "calendar", layout = MainLayout.class)
@PageTitle("Calendario de Citas")
@PermitAll
public class AppointmentCalendarView extends VerticalLayout {

  private final FullCalendar calendar;
  private final transient AppointmentEntryService appointmentEntryService;
  private final transient AppointmentService appointmentService;
  private final AppointmentForm appointmentForm;
  private final Span currentViewLabel;
  private final ComboBox<CalendarViewImpl> viewSelector;

  public AppointmentCalendarView(
      AppointmentEntryService appointmentEntryService,
      AppointmentService appointmentService,
      PetService petService,
      AuditService auditService,
      ClientService clientService,
      ClientMapper clientMapper) {
    this.appointmentEntryService = appointmentEntryService;
    this.appointmentService = appointmentService;

    appointmentForm =
        new AppointmentForm(
            appointmentService,
            clientService,
            petService,
            auditService,
            clientMapper,
            v -> loadAppointments());

    currentViewLabel = new Span();
    viewSelector = createViewSelector();

    addClassNames(
        LumoUtility.Background.CONTRAST_5,
        LumoUtility.Display.FLEX,
        LumoUtility.FlexDirection.COLUMN,
        LumoUtility.Padding.XSMALL);
    setSizeFull();
    setPadding(false);
    setSpacing(false);

    calendar =
        FullCalendarBuilder.create()
            .withCustomType(FullCalendarWithTooltip.class)
            .withAutoBrowserTimezone()
            .withAutoBrowserLocale()
            .withInitialOptions(createDefaultInitialOptions())
            .withEntryLimit(3)
            .withScheduler(Scheduler.GPL_V3_LICENSE_KEY)
            .build();

    configureCalendar();
    setupCalendarEventListeners();

    createCalendarContainer();

    HorizontalLayout header = createHeader();

    header.addClassNames(LumoUtility.Margin.Bottom.SMALL);
    add(header, calendar, appointmentForm);

    loadAppointments();
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
        new BusinessHours(
            LocalTime.of(8, 0), LocalTime.of(20, 0), BusinessHours.DEFAULT_BUSINESS_WEEK),
        new BusinessHours(LocalTime.of(9, 0), LocalTime.of(14, 0), DayOfWeek.SATURDAY));
  }

  private void setupCalendarEventListeners() {
    calendar.addEntryClickedListener(this::onEntryClick);
    calendar.addTimeslotsSelectedListener(this::onTimeslotsSelected);
    calendar.addEntryResizedListener(this::onEntryResized);
    calendar.addEntryDroppedListener(this::onEntryDropped);
    calendar.addDatesRenderedListener(this::onDatesRendered);
  }

  private HorizontalLayout createHeader() {
    HorizontalLayout header = new HorizontalLayout();
    header.setWidthFull();
    header.setPadding(false);
    header.setSpacing(false);
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    header.setAlignItems(FlexComponent.Alignment.CENTER);
    header.addClassNames(
        LumoUtility.Background.BASE,
        LumoUtility.BoxShadow.SMALL,
        LumoUtility.Padding.Vertical.MEDIUM,
        LumoUtility.Padding.Horizontal.MEDIUM,
        LumoUtility.Gap.MEDIUM);

    HorizontalLayout leftSection = createNavigationSection();

    HorizontalLayout centerSection = createCenterSection();

    HorizontalLayout rightSection = createActionSection();

    header.add(leftSection, centerSection, rightSection);
    return header;
  }

  private HorizontalLayout createNavigationSection() {
    Button prevBtn = new Button(VaadinIcon.ANGLE_LEFT.create());
    prevBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_CONTRAST);
    prevBtn.setTooltipText("Anterior");
    prevBtn.addClassNames(LumoUtility.BorderRadius.MEDIUM);
    prevBtn.addClickListener(e -> calendar.previous());

    Button nextBtn = new Button(VaadinIcon.ANGLE_RIGHT.create());
    nextBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_CONTRAST);
    nextBtn.setTooltipText("Siguiente");
    nextBtn.addClassNames(LumoUtility.BorderRadius.MEDIUM);
    nextBtn.addClickListener(e -> calendar.next());

    Button todayBtn = new Button("Hoy");
    todayBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
    todayBtn.addClassNames(LumoUtility.BorderRadius.MEDIUM);
    todayBtn.addClickListener(e -> calendar.today());

    HorizontalLayout navSection = new HorizontalLayout(prevBtn, todayBtn, nextBtn);
    navSection.setAlignItems(FlexComponent.Alignment.CENTER);
    navSection.addClassNames(LumoUtility.Gap.SMALL, LumoUtility.FlexWrap.NOWRAP);
    navSection.getStyle().set("flex-shrink", "0");

    return navSection;
  }

  private HorizontalLayout createCenterSection() {
    HorizontalLayout centerSection = new HorizontalLayout();
    centerSection.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
    centerSection.setAlignItems(FlexComponent.Alignment.CENTER);
    centerSection.addClassNames(LumoUtility.Flex.GROW);

    currentViewLabel.addClassNames(
        LumoUtility.FontSize.LARGE,
        LumoUtility.FontWeight.MEDIUM,
        LumoUtility.TextColor.SECONDARY,
        LumoUtility.TextAlignment.CENTER);

    centerSection.add(currentViewLabel);
    return centerSection;
  }

  private HorizontalLayout createActionSection() {
    Button newAppointmentBtn = new Button("Nueva Cita", createStyledIcon(VaadinIcon.PLUS));
    newAppointmentBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    newAppointmentBtn.addClassNames(LumoUtility.BorderRadius.MEDIUM);
    newAppointmentBtn.addClickListener(e -> openNewAppointmentDialog());

    Button mobileNewBtn = new Button(createStyledIcon(VaadinIcon.PLUS));
    mobileNewBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ICON);
    mobileNewBtn.setTooltipText("Nueva Cita");
    mobileNewBtn.addClassNames(LumoUtility.BorderRadius.MEDIUM);
    mobileNewBtn.getStyle().set("display", "none");
    mobileNewBtn.addClickListener(e -> openNewAppointmentDialog());

    Button refreshBtn = new Button(createStyledIcon(VaadinIcon.REFRESH));
    refreshBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_CONTRAST);
    refreshBtn.setTooltipText("Actualizar");
    refreshBtn.addClassNames(LumoUtility.BorderRadius.MEDIUM);
    refreshBtn.addClickListener(e -> loadAppointments());

    viewSelector.addClassNames(LumoUtility.BorderRadius.MEDIUM);
    viewSelector.setWidth("180px");
    viewSelector.getStyle().set("min-width", "150px");

    HorizontalLayout actionSection =
        new HorizontalLayout(mobileNewBtn, refreshBtn, viewSelector, newAppointmentBtn);
    actionSection.setAlignItems(FlexComponent.Alignment.CENTER);
    actionSection.addClassNames(LumoUtility.Gap.SMALL, LumoUtility.FlexWrap.NOWRAP);
    actionSection.getStyle().set("flex-shrink", "0");

    return actionSection;
  }

  private void createCalendarContainer() {
    calendar.addClassNames(
        LumoUtility.Background.BASE,
        LumoUtility.BorderRadius.MEDIUM,
        LumoUtility.BoxShadow.SMALL,
        LumoUtility.Padding.MEDIUM);
    calendar.setSizeFull();
    setFlexGrow(1, calendar);
  }

  private ComboBox<CalendarViewImpl> createViewSelector() {
    ComboBox<CalendarViewImpl> selector = new ComboBox<>();
    selector.setItems(
        CalendarViewImpl.DAY_GRID_MONTH,
        CalendarViewImpl.TIME_GRID_WEEK,
        CalendarViewImpl.TIME_GRID_DAY,
        CalendarViewImpl.LIST_WEEK);
    selector.setItemLabelGenerator(this::getViewDisplayName);
    selector.setAllowCustomValue(false);
    selector.setValue(CalendarViewImpl.DAY_GRID_MONTH);
    selector.addValueChangeListener(
        e -> {
          CalendarViewImpl value = e.getValue();
          if (value != null) {
            calendar.changeView(value);
          }
        });
    return selector;
  }

  private Icon createStyledIcon(VaadinIcon vaadinIcon) {
    Icon icon = vaadinIcon.create();
    icon.addClassNames(LumoUtility.IconSize.SMALL);
    return icon;
  }

  private void onDatesRendered(DatesRenderedEvent event) {
    LocalDate intervalStart = event.getIntervalStart();
    LocalDate intervalEnd = event.getIntervalEnd();
    CalendarViewImpl currentView = viewSelector.getValue();

    if (currentView != null && intervalStart != null) {
      String formattedInterval = formatIntervalForView(intervalStart, currentView);
      currentViewLabel.setText(formattedInterval);

      if (intervalEnd != null) {
        loadAppointmentsForDateRange(intervalStart, intervalEnd);
      }
    }
  }

  private String getViewDisplayName(CalendarViewImpl view) {
    return switch (view) {
      case DAY_GRID_MONTH -> "Vista Mensual";
      case TIME_GRID_WEEK -> "Vista Semanal";
      case TIME_GRID_DAY -> "Vista Diaria";
      case LIST_WEEK -> "Lista Semanal";
      default -> view.getClientSideValue();
    };
  }

  private String formatIntervalForView(LocalDate intervalStart, CalendarViewImpl view) {
    Locale locale = Locale.of("es", "ES");
    DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale);
    DateTimeFormatter dayFormatter =
        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", locale);

    return switch (view) {
      case DAY_GRID_MONTH -> intervalStart.format(monthFormatter).toUpperCase();
      case TIME_GRID_WEEK -> {
        LocalDate weekEnd = intervalStart.plusDays(6);
        if (intervalStart.getMonth() == weekEnd.getMonth()) {
          yield String.format(
              "%d - %d de %s %d",
              intervalStart.getDayOfMonth(),
              weekEnd.getDayOfMonth(),
              intervalStart.getMonth().getDisplayName(TextStyle.FULL, locale),
              intervalStart.getYear());
        } else {
          yield String.format(
              "%d de %s - %d de %s %d",
              intervalStart.getDayOfMonth(),
              intervalStart.getMonth().getDisplayName(TextStyle.SHORT, locale),
              weekEnd.getDayOfMonth(),
              weekEnd.getMonth().getDisplayName(TextStyle.SHORT, locale),
              weekEnd.getYear());
        }
      }
      case TIME_GRID_DAY -> intervalStart.format(dayFormatter);
      case LIST_WEEK -> {
        LocalDate weekEnd = intervalStart.plusDays(6);
        yield String.format(
            "Semana del %d al %d de %s %d",
            intervalStart.getDayOfMonth(),
            weekEnd.getDayOfMonth(),
            intervalStart.getMonth().getDisplayName(TextStyle.FULL, locale),
            intervalStart.getYear());
      }
      default -> intervalStart.toString();
    };
  }

  private void openNewAppointmentDialog() {
    appointmentForm.openForNew(LocalDateTime.now(), LocalDateTime.now().plusHours(1));
  }

  public void loadAppointments() {
    try {
      CallbackEntryProvider<Entry> entryProvider =
          EntryProvider.fromCallbacks(
              query -> {
                LocalDate start = query.getStart().toLocalDate();
                LocalDate end = query.getEnd().toLocalDate();
                LocalDate bufferedStart = start.minusDays(7);
                LocalDate bufferedEnd = end.plusDays(7);

                return appointmentEntryService
                    .getAppointmentEntriesInRange(
                        bufferedStart.atStartOfDay(), bufferedEnd.atTime(23, 59, 59))
                    .stream();
              },
              entryId ->
                  appointmentEntryService
                      .getAppointment(Long.valueOf(entryId))
                      .map(appointmentEntryService::convertToEntry)
                      .orElse(null));

      calendar.setEntryProvider(entryProvider);
      calendar.getEntryProvider().refreshAll();
    } catch (Exception e) {
      log.error("Error loading appointments", e);
    }
  }

  private void loadAppointmentsForDateRange(LocalDate start, LocalDate end) {
    try {
      LocalDate bufferedStart = start.minusDays(3);
      LocalDate bufferedEnd = end.plusDays(3);

      List<Entry> entries =
          appointmentEntryService.getAppointmentEntriesInRange(
              bufferedStart.atStartOfDay(), bufferedEnd.atTime(23, 59, 59));

      org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider<Entry> entryProvider =
          EntryProvider.inMemoryFrom(entries);
      calendar.setEntryProvider(entryProvider);
      calendar.getEntryProvider().refreshAll();
    } catch (Exception e) {
      log.error("Error loading appointments for date range: {} to {}", start, end, e);
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
    appointmentEntryService.getAppointment(appointmentId).ifPresent(appointmentForm::openForEdit);
  }

  private void onTimeslotsSelected(TimeslotsSelectedEvent event) {
    LocalDateTime start = event.getStart();
    LocalDateTime end = event.getEnd();
    appointmentForm.openForNew(start, end);
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

      Notification notification = Notification.show("Cita actualizada correctamente");
      notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
      notification.setPosition(Notification.Position.TOP_END);
      notification.setDuration(3000);
    } catch (ConstraintViolationException e) {
      log.error("Validation error updating appointment", e);
      ValidationNotificationUtils.handleCalendarValidationErrors(e);
      loadAppointments();
    } catch (Exception e) {
      log.error("Error updating appointment", e);

      Notification notification =
          Notification.show("Error al actualizar la cita. Inténtelo de nuevo.");
      notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
      notification.setPosition(Notification.Position.TOP_CENTER);
      notification.setDuration(5000);

      loadAppointments();
    }
  }

  private JsonObject createDefaultInitialOptions() {
    JsonObject initialOptions = Json.createObject();

    initialOptions.put("headerToolbar", false);

    initialOptions.put("height", "auto");
    initialOptions.put("weekNumbers", true);
    initialOptions.put("weekNumberCalculation", "ISO");
    initialOptions.put("navLinks", true);
    initialOptions.put("dayMaxEvents", 3);
    initialOptions.put("moreLinkClick", "popover");
    initialOptions.put("eventDisplay", "block");

    initialOptions.put(
        "eventTimeFormat",
        Json.parse("{\"hour\": \"2-digit\", \"minute\": \"2-digit\", \"meridiem\": false}"));

    initialOptions.put(
        "slotLabelFormat",
        Json.parse("{\"hour\": \"2-digit\", \"minute\": \"2-digit\", \"meridiem\": false}"));

    initialOptions.put("locale", "es");
    initialOptions.put("firstDay", 1);

    initialOptions.put("selectable", true);
    initialOptions.put("selectMirror", true);
    initialOptions.put("editable", true);
    initialOptions.put("eventResizableFromStart", true);

    initialOptions.put("handleWindowResize", true);
    initialOptions.put("windowResizeDelay", 100);

    return initialOptions;
  }
}
