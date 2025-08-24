package com.wornux.views.calendar.utils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurrentDate {

  private LocalDate date;
  private YearMonth yearMonth;
  private String timeZone;

  private CurrentDate() {}

  public CurrentDate(String timeZone) {
    this.timeZone = timeZone;
    date = LocalDate.now(ZoneId.of(timeZone));
  }

  public void next(int count) {
    setDate(date.plusDays(count));
  }

  public void previous(int count) {
    setDate(date.minusDays(count));
  }

  public void today() {
    date = LocalDate.now(ZoneId.of(timeZone));
  }
}
