package com.wornux.validation.workschedule;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = WorkScheduleValidator.class)
@Documented
public @interface ValidWorkSchedule {
  String message() default "Invalid work schedule";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}