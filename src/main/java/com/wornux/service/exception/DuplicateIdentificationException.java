package com.wornux.exception;

public class DuplicateIdentificationException extends RuntimeException {
    public DuplicateIdentificationException(String type, String value) {
        super("Ya existe un cliente con " + type + ": " + value);
    }
}