package com.wornux.dto.response;

import com.wornux.data.enums.PetType;
import java.time.LocalDate;

public record PetSummaryResponseDto(Long id, String name, PetType type, String breed, LocalDate birthDate, String ownerName) {
}
