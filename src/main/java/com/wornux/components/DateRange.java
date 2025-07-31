package com.wornux.components;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class DateRange {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public DateRange(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

}
