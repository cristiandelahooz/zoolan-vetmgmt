package com.zoolandia.app.features.waitingRoom.service.exception;

public class WaitingRoomNotFoundException extends RuntimeException {

    public WaitingRoomNotFoundException(Long id) {
        super("Entrada de sala de espera no encontrada con ID: " + id);
    }

    public WaitingRoomNotFoundException(String message) {
        super(message);
    }
}
