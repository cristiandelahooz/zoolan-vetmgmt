package com.wornux.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EmployeeCredentialsException extends EmployeeException {
  public EmployeeCredentialsException(String message) {
    super(message);
  }

  public EmployeeCredentialsException(String field, String requirement) {
    super(String.format("Invalid %s: %s", field, requirement));
  }
}
