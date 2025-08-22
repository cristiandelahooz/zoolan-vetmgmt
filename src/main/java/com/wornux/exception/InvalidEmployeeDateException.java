package com.wornux.exception;

import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidEmployeeDateException extends EmployeeException {
  public InvalidEmployeeDateException(String message) {
    super(message);
  }

  public InvalidEmployeeDateException(LocalDate hireDate, LocalDate birthDate) {
    super(
        String.format(
            "Invalid date combination: hire date %s cannot be before birth date %s",
            hireDate, birthDate));
  }
}
