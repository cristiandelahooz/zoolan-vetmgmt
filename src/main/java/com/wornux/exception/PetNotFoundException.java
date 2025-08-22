package com.wornux.exception;

public class PetNotFoundException extends RuntimeException {
    public PetNotFoundException(Long id) {
        super("Mascota no encontrada con ID: " + id);
    }
}
