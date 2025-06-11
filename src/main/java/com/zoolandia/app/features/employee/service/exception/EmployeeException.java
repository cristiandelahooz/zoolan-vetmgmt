package com.zoolandia.app.features.employee.service.exception;

public abstract class EmployeeException extends RuntimeException {
    protected EmployeeException(String message) {
        super(message);
    }

    protected EmployeeException(String message, Throwable cause) {
        super(message, cause);
    }
}