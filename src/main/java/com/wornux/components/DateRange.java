package com.wornux.components;

import java.time.LocalDate;
import lombok.Getter;

@Getter
public class DateRange {

  private final LocalDate startDate;
  private final LocalDate endDate;

  public DateRange(LocalDate startDate, LocalDate endDate) {
    this.startDate = startDate;
    this.endDate = endDate;
  }
}
