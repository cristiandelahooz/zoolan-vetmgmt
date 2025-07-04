package com.wornux.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidEmployeeRoleException extends EmployeeException {
    public InvalidEmployeeRoleException(String role) {
        super(String.format("Invalid employee role: %s", role));
    }

    public InvalidEmployeeRoleException(String role, String requiredRole) {
        super(String.format("Employee role %s is not authorized. Required role: %s", role, requiredRole));
    }
}