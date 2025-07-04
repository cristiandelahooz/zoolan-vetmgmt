package com.wornux.dto;

import com.wornux.domain.Priority;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

public record WaitingRoomCreateDTO(@NotNull(message = "El cliente es requerido") Long clientId,

        @NotNull(message = "La mascota es requerida") Long petId,

        @NotNull(message = "La razón de la visita es requerida") String reasonForVisit,

        @NotNull(message = "La prioridad es requerida") Priority priority,

        @Nullable String notes) {
}