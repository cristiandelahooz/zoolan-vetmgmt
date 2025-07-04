package com.wornux.dto;

import com.wornux.domain.PetType;
import java.time.LocalDate;

public record PetSummaryDTO(Long id, String name, PetType type, String breed, LocalDate birthDate, String ownerName) {
}
