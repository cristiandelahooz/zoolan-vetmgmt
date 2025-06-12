package com.zoolandia.app.features.employee.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class EmployeeOperationNotAllowedException extends EmployeeException {
    public EmployeeOperationNotAllowedException(String operation, String reason) {
        super(String.format("Operation '%s' not allowed: %s", operation, reason));
    }

    public EmployeeOperationNotAllowedException(Long employeeId, String operation) {
        super(String.format("Operation '%s' not allowed for employee ID %d", operation, employeeId));
    }
}