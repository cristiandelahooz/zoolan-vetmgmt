package com.zoolandia.app.features.pet.service.dto;

import com.zoolandia.app.features.pet.domain.PetBreed;
import com.zoolandia.app.features.pet.domain.PetType;

import java.time.LocalDate;

public record PetSummaryDTO(Long id, String name, PetType type, PetBreed breed, LocalDate birthDate, String ownerName) {
}
