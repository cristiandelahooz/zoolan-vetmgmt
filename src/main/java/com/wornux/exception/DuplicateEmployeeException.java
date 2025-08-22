package com.wornux.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEmployeeException extends EmployeeException {
    public DuplicateEmployeeException(String field, String value) {
        super(String.format("Empleado con %s '%s' ya existe", field, value));
    }
}
