package com.wornux.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.*;
import org.jspecify.annotations.Nullable;

/** Embeddable entity representing a single day's work schedule */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class WorkScheduleDay {

  @Enumerated(EnumType.STRING)
  @Column(name = "day_of_week")
  @NotNull
  private DayOfWeek dayOfWeek;

  @Column(name = "start_time")
  @Nullable
  private LocalTime startTime;

  @Column(name = "end_time")
  @Nullable
  private LocalTime endTime;

  @Column(name = "is_off_day")
  @Builder.Default
  private boolean isOffDay = false;

  /** Validates that start time is before end time */
  public boolean isValidTimeRange() {
    if (isOffDay) {
      return true;
    }
    if (startTime == null || endTime == null) {
      return false;
    }
    return startTime.isBefore(endTime);
  }

  /** Returns a formatted string representation of the schedule */
  @Override
  public String toString() {
    if (isOffDay) {
      return dayOfWeek.name() + ": Off";
    }
    if (startTime != null && endTime != null) {
      return dayOfWeek.name() + ": " + startTime + " - " + endTime;
    }
    return dayOfWeek.name() + ": Not set";
  }
}
