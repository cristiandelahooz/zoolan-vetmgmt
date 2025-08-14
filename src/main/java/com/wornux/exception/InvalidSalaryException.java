package com.wornux.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidSalaryException extends EmployeeException {
  public InvalidSalaryException(Double salary) {
    super(String.format("Invalid salary amount: %.2f", salary));
  }

  public InvalidSalaryException(Double salary, Double minimumSalary) {
    super(String.format("Salary %.2f is below minimum allowed: %.2f", salary, minimumSalary));
  }
}
