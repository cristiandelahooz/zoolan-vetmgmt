package com.wornux.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * DTO for WorkScheduleDay used in create/update requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkScheduleDayDto {

  @NotNull(message = "Day of week is required")
  private DayOfWeek dayOfWeek;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
  private LocalTime startTime;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
  private LocalTime endTime;

  @Builder.Default
  private boolean isOffDay = false;

  /**
   * Validates that start time is before end time
   */
  public boolean isValidTimeRange() {
    if (isOffDay) {
      return true;
    }
    if (startTime == null || endTime == null) {
      return false;
    }
    return startTime.isBefore(endTime);
  }
}