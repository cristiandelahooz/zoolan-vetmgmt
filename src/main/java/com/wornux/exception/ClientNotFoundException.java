package com.wornux.exception;

public class ClientNotFoundException extends RuntimeException {
  public ClientNotFoundException(Long id) {
    super("Cliente no encontrado con ID: " + id);
  }
}
