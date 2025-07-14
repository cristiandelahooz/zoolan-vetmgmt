package com.wornux.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long id) {
        super("Producto no encontrado con ID: " + id);
    }
}
