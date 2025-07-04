package com.wornux.features.pet.service.exception;

public class OwnerNotFoundException extends RuntimeException {
    public OwnerNotFoundException(Long ownerId) {
        super("No se encontró un cliente con el ID: " + ownerId);
    }
}
