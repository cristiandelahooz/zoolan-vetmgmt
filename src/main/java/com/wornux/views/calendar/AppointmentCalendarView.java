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
import com.wornux.services.interfaces.AppointmentService;
import com.wornux.services.interfaces.PetService;
import com.wornux.utils.ValidationNotificationUtils;
import com.wornux.views.MainLayout;
import com.wornux.views.calendar.utils.FullCalendarWithTooltip;
import elemental.json.Json;
import elemental.json.JsonObject;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.ConstraintViolationException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;

@Slf4j
@Route(value = "calendar", layout = MainLayout.class)
@PageTitle("Calendario de Citas")
@PermitAll
public class AppointmentCalendarView extends VerticalLayout {

  private final FullCalendar calendar;
  private final transient AppointmentEntryService appointmentEntryService;
  private final transient AppointmentService appointmentService;
  private final AppointmentDialog appointmentDialog;
  private final Span currentViewLabel;
  private final ComboBox<CalendarViewImpl> viewSelector;

  public AppointmentCalendarView(
      AppointmentEntryService appointmentEntryService,
      AppointmentService appointmentService,
      PetService petService) {
    this.appointmentEntryService = appointmentEntryService;
    this.appointmentService = appointmentService;

    appointmentDialog =
        new AppointmentDialog(appointmentService, petService, v -> loadAppointments());

    // Initialize UI components
    currentViewLabel = new Span();
    viewSelector = createViewSelector();

    // Layout configuration with professional styling
    addClassNames(
        LumoUtility.Background.CONTRAST_5,
        LumoUtility.Display.FLEX,
        LumoUtility.FlexDirection.COLUMN,
        LumoUtility.Padding.MEDIUM);
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
            .build();

    configureCalendar();
    setupCalendarEventListeners();

    // Create calendar container with proper styling
    createCalendarContainer();

    // Create header and toolbar
    VerticalLayout header = createHeader();

    // Layout composition with spacing
    header.addClassNames(LumoUtility.Margin.Bottom.SMALL);
    add(header, calendar);

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

  private VerticalLayout createHeader() {
    VerticalLayout header = new VerticalLayout();
    header.setPadding(false);
    header.setSpacing(false);
    header.addClassNames(
        LumoUtility.Background.BASE, LumoUtility.BoxShadow.SMALL, LumoUtility.Padding.LARGE);

    // Main toolbar
    HorizontalLayout toolbar = createMainToolbar();

    // View info section
    HorizontalLayout viewInfo = createViewInfoSection();

    header.add(toolbar, viewInfo);
    return header;
  }

  private HorizontalLayout createMainToolbar() {
    // Navigation buttons with professional styling
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

    // Action buttons group - responsive design
    Button newAppointmentBtn = new Button("Nueva Cita", createStyledIcon(VaadinIcon.PLUS));
    newAppointmentBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    newAppointmentBtn.addClassNames(LumoUtility.BorderRadius.MEDIUM);

    // Mobile-friendly button with icon only
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

    // Navigation group - responsive
    HorizontalLayout navGroup = new HorizontalLayout(prevBtn, todayBtn, nextBtn);
    navGroup.addClassNames(
        LumoUtility.Gap.SMALL, LumoUtility.AlignItems.CENTER, LumoUtility.FlexWrap.NOWRAP);

    // Action group - responsive
    HorizontalLayout actionGroup =
        new HorizontalLayout(newAppointmentBtn, mobileNewBtn, refreshBtn);
    actionGroup.addClassNames(
        LumoUtility.Gap.SMALL, LumoUtility.AlignItems.CENTER, LumoUtility.FlexWrap.NOWRAP);
    newAppointmentBtn.addClickListener(e -> openNewAppointmentDialog());

    // Main toolbar layout with responsive behavior
    HorizontalLayout toolbar = new HorizontalLayout();
    toolbar.setWidthFull();
    toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
    toolbar.addClassNames(LumoUtility.Gap.MEDIUM, LumoUtility.Padding.Vertical.MEDIUM);

    toolbar.add(navGroup, actionGroup);
    return toolbar;
  }

  private HorizontalLayout createViewInfoSection() {
    // Current period label with professional styling and responsive behavior
    currentViewLabel.addClassNames(
        LumoUtility.FontSize.LARGE,
        LumoUtility.FontWeight.MEDIUM,
        LumoUtility.TextColor.SECONDARY,
        LumoUtility.Overflow.HIDDEN);
    currentViewLabel.getStyle().set("flex-grow", "1");

    // View selector styling with responsive width
    viewSelector.addClassNames(LumoUtility.BorderRadius.MEDIUM);
    viewSelector.setWidth("180px");
    viewSelector.getStyle().set("min-width", "150px");
    viewSelector.getStyle().set("flex-shrink", "0");

    HorizontalLayout viewInfo = new HorizontalLayout(currentViewLabel, viewSelector);
    viewInfo.setWidthFull();
    viewInfo.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    viewInfo.setAlignItems(FlexComponent.Alignment.CENTER);
    viewInfo.addClassNames(
        LumoUtility.Gap.MEDIUM, LumoUtility.Padding.Vertical.SMALL, LumoUtility.Padding.Top.MEDIUM);
    viewInfo.getStyle().set("border-top", "1px solid var(--lumo-contrast-10pct)");

    return viewInfo;
  }

  private void createCalendarContainer() {
    calendar.addClassNames(
        LumoUtility.Background.BASE,
        LumoUtility.BorderRadius.MEDIUM,
        LumoUtility.BoxShadow.SMALL,
        LumoUtility.Padding.MEDIUM,
        LumoUtility.Overflow.HIDDEN);
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
    CalendarViewImpl currentView = viewSelector.getValue();

    if (currentView != null && intervalStart != null) {
      String formattedInterval = formatIntervalForView(intervalStart, currentView);
      currentViewLabel.setText(formattedInterval);
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
    appointmentEntryService.getAppointment(appointmentId).ifPresent(appointmentDialog::openForEdit);
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

      // Professional styled notification
      Notification notification = Notification.show("Cita actualizada correctamente");
      notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
      notification.setPosition(Notification.Position.TOP_END);
      notification.setDuration(3000);
    } catch (ConstraintViolationException e) {
      log.error("Validation error updating appointment", e);
      ValidationNotificationUtils.handleCalendarValidationErrors(e);
      loadAppointments(); // Reload to revert visual changes
    } catch (Exception e) {
      log.error("Error updating appointment", e);

      // Professional styled error notification
      Notification notification =
          Notification.show("Error al actualizar la cita. Inténtelo de nuevo.");
      notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
      notification.setPosition(Notification.Position.TOP_CENTER);
      notification.setDuration(5000);

      loadAppointments(); // Reload to revert visual changes
    }
  }

  private JsonObject createDefaultInitialOptions() {
    JsonObject initialOptions = Json.createObject();

    // Hide default header as we create custom toolbar
    initialOptions.put("headerToolbar", false);

    // Professional calendar settings
    initialOptions.put("height", "auto");
    initialOptions.put("weekNumbers", true);
    initialOptions.put("weekNumberCalculation", "ISO");
    initialOptions.put("navLinks", true);
    initialOptions.put("dayMaxEvents", 3);
    initialOptions.put("moreLinkClick", "popover");
    initialOptions.put("eventDisplay", "block");

    // Time formatting
    initialOptions.put(
        "eventTimeFormat",
        Json.parse("{\"hour\": \"2-digit\", \"minute\": \"2-digit\", \"meridiem\": false}"));

    initialOptions.put(
        "slotLabelFormat",
        Json.parse("{\"hour\": \"2-digit\", \"minute\": \"2-digit\", \"meridiem\": false}"));

    // Spanish localization with better formatting
    initialOptions.put("locale", "es");
    initialOptions.put("firstDay", 1); // Monday

    // Enhanced interaction
    initialOptions.put("selectable", true);
    initialOptions.put("selectMirror", true);
    initialOptions.put("editable", true);
    initialOptions.put("eventResizableFromStart", true);

    // Better mobile experience
    initialOptions.put("handleWindowResize", true);
    initialOptions.put("windowResizeDelay", 100);

    return initialOptions;
  }
}
