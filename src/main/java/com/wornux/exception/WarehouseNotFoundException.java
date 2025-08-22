package com.wornux.exception;

public class WarehouseNotFoundException extends RuntimeException {
    public WarehouseNotFoundException(Long id) {
        super("Warehouse not found with id: " + id);
    }

    public WarehouseNotFoundException(String name) {
        super("Warehouse not found with name: " + name);
    }

    public WarehouseNotFoundException() {
        super("Warehouse not found");
    }
}
