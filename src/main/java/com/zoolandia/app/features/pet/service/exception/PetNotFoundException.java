package com.zoolandia.app.features.pet.service.exception;

public class PetNotFoundException extends RuntimeException {
  public PetNotFoundException(Long id) {
    super("Mascota no encontrada con ID: " + id);
  }
}
