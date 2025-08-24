package com.wornux.views.calendar;

import com.wornux.views.calendar.entryproviders.EntryService;
import elemental.json.JsonObject;
import java.util.Collections;
import org.vaadin.stefan.fullcalendar.*;

public class BasicDemo extends AbstractCalendarView {

  @Override
  protected FullCalendar createCalendar(JsonObject defaultInitialOptions) {
    EntryService<Entry> simpleInstance = EntryService.createSimpleInstance();

    return FullCalendarBuilder.create()
        .withInitialOptions(defaultInitialOptions)
        .withInitialEntries(simpleInstance.getEntries())
        .withEntryLimit(3)
        .withScheduler("GPL-My-Project-Is-Open-Source")
        .build();
  }

  @Override
  protected void onEntryClick(EntryClickedEvent event) {
    System.out.println(event.getClass().getSimpleName() + ": " + event);

    if (event.getEntry().getDisplayMode() != DisplayMode.BACKGROUND
        && event.getEntry().getDisplayMode() != DisplayMode.INVERSE_BACKGROUND) {
      DemoDialog dialog = new DemoDialog(event.getEntry(), false);
      dialog.setSaveConsumer(this::onEntryChanged);
      dialog.setDeleteConsumer(e -> onEntriesRemoved(Collections.singletonList(e)));
      dialog.open();
    }
  }

  @Override
  protected void onTimeslotsSelected(TimeslotsSelectedEvent event) {
    super.onTimeslotsSelected(event);

    ResourceEntry entry = new ResourceEntry();

    entry.setStart(event.getStart());
    entry.setEnd(event.getEnd());
    entry.setAllDay(event.isAllDay());
    entry.setCalendar(event.getSource());

    DemoDialog dialog = new DemoDialog(entry, true);
    dialog.setSaveConsumer(e -> onEntriesCreated(Collections.singletonList(e)));
    dialog.setDeleteConsumer(e -> onEntriesRemoved(Collections.singletonList(e)));
    dialog.open();
  }
}
