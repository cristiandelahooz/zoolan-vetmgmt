package com.zoolandia.app.features.employee.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEmployeeException extends EmployeeException {
    public DuplicateEmployeeException(String field, String value) {
        super(String.format("Employee with %s '%s' already exists", field, value));
    }
}