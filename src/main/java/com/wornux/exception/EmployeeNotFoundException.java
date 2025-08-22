package com.wornux.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EmployeeNotFoundException extends EmployeeException {
    public EmployeeNotFoundException(Long id) {
        super(String.format("Employee with ID %d not found", id));
    }

    public EmployeeNotFoundException(String username) {
        super(String.format("Employee with username %s not found", username));
    }
}
