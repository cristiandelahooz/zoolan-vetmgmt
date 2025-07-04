package com.wornux.features.pet.service.dto;

import com.wornux.features.pet.domain.PetType;

import java.time.LocalDate;

public record PetSummaryDTO(Long id, String name, PetType type, String breed, LocalDate birthDate, String ownerName) {
}
