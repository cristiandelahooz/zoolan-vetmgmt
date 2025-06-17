package com.zoolandia.app.features.client.service.exception;

public class DuplicateIdentificationException extends RuntimeException {
    public DuplicateIdentificationException(String type, String value) {
        super("Ya existe un cliente con " + type + ": " + value);
    }
}