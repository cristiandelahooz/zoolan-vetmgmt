package com.wornux.exception;

public class InvalidIdentificationException extends RuntimeException {
    public InvalidIdentificationException() {
        super("Debe proporcionar al menos cédula o pasaporte");
    }
}
