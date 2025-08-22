package com.wornux.validation.workschedule;

import com.wornux.dto.request.WorkScheduleDayDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WorkScheduleValidator
    implements ConstraintValidator<ValidWorkSchedule, List<WorkScheduleDayDto>> {

  @Override
  public boolean isValid(
      List<WorkScheduleDayDto> workScheduleDays, ConstraintValidatorContext context) {
    if (workScheduleDays == null || workScheduleDays.isEmpty()) {
      return true; // Allow empty schedules
    }

    // Check for duplicate days
    Set<DayOfWeek> days =
        workScheduleDays.stream().map(WorkScheduleDayDto::getDayOfWeek).collect(Collectors.toSet());

    if (days.size() != workScheduleDays.size()) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate("Duplicate days are not allowed")
          .addConstraintViolation();
      return false;
    }

    // Validate each day's time range
    for (WorkScheduleDayDto day : workScheduleDays) {
      if (!day.isValidTimeRange()) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate("Invalid time range for " + day.getDayOfWeek())
            .addConstraintViolation();
        return false;
      }
    }

    return true;
  }
}
