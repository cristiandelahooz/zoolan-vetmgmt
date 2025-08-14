package com.wornux.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.shared.Registration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CssImport("./themes/zoolan-vetmgmt/components/minicalendar.css")
public class MiniCalendarRange extends CustomField<DateRange>
    implements HasThemeVariant<MiniCalendarVariant>, LocaleChangeObserver {

  private static final String CSS_BASE = "minicalendar";
  private static final String CSS_DAY = "day";
  private static final String CSS_SELECTED = "selected";
  private static final String CSS_RANGE = "range";

  private final VerticalLayout content = new VerticalLayout();
  private final Map<LocalDate, Component> dayToComponentMapping = new HashMap<>(31);
  private final YearMonthHolder yearMonthHolder = new YearMonthHolder();
  private LocalDate startDate = null;
  private LocalDate endDate = null;

  public MiniCalendarRange() {
    this(YearMonth.now());
  }

  public MiniCalendarRange(YearMonth yearMonth) {
    yearMonthHolder.setValue(yearMonth);
    yearMonthHolder.addValueChangeListener(e -> redraw());

    content.addClassName(CSS_BASE);
    content.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
    content.setSpacing(false);
    content.setPadding(false);
    content.setMargin(false);
    add(content);

    renderComponent();
  }

  @Override
  protected DateRange generateModelValue() {
    if (startDate == null || endDate == null) {
      return null;
    }
    return new DateRange(startDate, endDate);
  }

  @Override
  protected void setPresentationValue(DateRange dateRange) {
    if (dateRange != null) {
      this.startDate = dateRange.getStartDate();
      this.endDate = dateRange.getEndDate();
      applyRangeStyles();
    } else {
      this.startDate = null;
      this.endDate = null;
      clearRangeStyles();
    }
  }

  private void redraw() {
    resetComponent();
    renderComponent();
  }

  private void resetComponent() {
    content.removeAll();
    dayToComponentMapping.clear();
  }

  private void renderComponent() {
    renderHeader();
    renderDays();
  }

  private void renderHeader() {
    Button previousMonthButton = new Button("<", e -> navigateToPreviousMonth());
    previousMonthButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

    Button nextMonthButton = new Button(">", e -> navigateToNextMonth());
    nextMonthButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

    Span monthTitle =
        new Span(yearMonthHolder.getValue().getMonth().getDisplayName(TextStyle.FULL, getLocale()));
    Span yearTitle = new Span(String.valueOf(yearMonthHolder.getValue().getYear()));

    HorizontalLayout header =
        new HorizontalLayout(previousMonthButton, monthTitle, yearTitle, nextMonthButton);
    header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
    header.setWidthFull();
    header.setSpacing(true);
    content.add(header);
  }

  private void renderDays() {
    YearMonth yearMonth = yearMonthHolder.getValue();
    LocalDate firstDayOfMonth = yearMonth.atDay(1);
    LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();

    List<Component> dayComponents = new ArrayList<>();
    LocalDate current = firstDayOfMonth;

    while (!current.isAfter(lastDayOfMonth)) {
      Component dayComponent = createDayComponent(current);
      dayToComponentMapping.put(current, dayComponent);
      dayComponents.add(dayComponent);

      if (dayComponents.size() == 7) {
        addRow(dayComponents);
        dayComponents.clear();
      }

      current = current.plusDays(1);
    }

    if (!dayComponents.isEmpty()) {
      addRow(dayComponents);
    }
  }

  private Component createDayComponent(LocalDate date) {
    Span dayComponent = new Span(String.valueOf(date.getDayOfMonth()));
    dayComponent.addClassName(CSS_DAY);

    dayComponent.addClickListener(
        event -> {
          if (startDate == null || (startDate != null && endDate != null)) {
            startDate = date;
            endDate = null;
            clearRangeStyles();
          } else if (startDate != null) {
            endDate = date;
            if (endDate.isBefore(startDate)) {
              LocalDate temp = startDate;
              startDate = endDate;
              endDate = temp;
            }
            applyRangeStyles();
          }

          setModelValue(generateModelValue(), true);
        });

    return dayComponent;
  }

  private void addRow(List<Component> components) {
    FlexLayout row = new FlexLayout();
    row.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
    row.setWidthFull();
    row.add(components.toArray(new Component[0]));
    content.add(row);
  }

  private void applyRangeStyles() {
    if (startDate == null || endDate == null) {
      return;
    }

    LocalDate current = startDate;
    while (!current.isAfter(endDate)) {
      Component dayComponent = dayToComponentMapping.get(current);
      if (dayComponent != null) {
        dayComponent.addClassName(CSS_RANGE);
      }
      current = current.plusDays(1);
    }
  }

  private void clearRangeStyles() {
    dayToComponentMapping.values().forEach(component -> component.removeClassName(CSS_RANGE));
  }

  private void navigateToPreviousMonth() {
    yearMonthHolder.setValue(yearMonthHolder.getValue().minusMonths(1));
  }

  private void navigateToNextMonth() {
    yearMonthHolder.setValue(yearMonthHolder.getValue().plusMonths(1));
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    redraw();
  }

  private static class YearMonthHolder {
    private final List<ValueChangeListener<? super ValueChangeEvent<YearMonth>>> listeners =
        new ArrayList<>();
    private YearMonth value;

    public YearMonth getValue() {
      return value;
    }

    public void setValue(YearMonth value) {
      YearMonth oldValue = this.value;
      this.value = value;
      listeners.forEach(
          listener ->
              listener.valueChanged(
                  new ValueChangeEvent<>() {
                    @Override
                    public HasValue<?, YearMonth> getHasValue() {
                      return null;
                    }

                    @Override
                    public boolean isFromClient() {
                      return false;
                    }

                    @Override
                    public YearMonth getOldValue() {
                      return oldValue;
                    }

                    @Override
                    public YearMonth getValue() {
                      return value;
                    }
                  }));
    }

    public Registration addValueChangeListener(
        ValueChangeListener<? super ValueChangeEvent<YearMonth>> listener) {
      listeners.add(listener);
      return () -> listeners.remove(listener);
    }
  }
}
