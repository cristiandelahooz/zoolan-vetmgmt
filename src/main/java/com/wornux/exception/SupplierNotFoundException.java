package com.wornux.exception;

public class SupplierNotFoundException extends RuntimeException {
    public SupplierNotFoundException(Long id) {
        super("Proveedor no encontrado con ID: " + id);
    }
}
