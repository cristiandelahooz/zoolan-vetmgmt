package com.wornux.views.calendar;

import java.time.LocalDate;
import java.util.Locale;

/**
 * @author Stefan Uebe
 */
@FunctionalInterface
public interface HasIntervalLabel {

  String formatIntervalLabel(LocalDate intervalStart, Locale locale);
}
