package com.wornux.dto.response;

import com.wornux.data.enums.PetType;
import com.wornux.data.enums.PetSize;
import com.wornux.data.enums.FurType;

import java.time.LocalDate;

public record PetSummaryResponseDto(Long id, String name, PetType type, String breed, LocalDate birthDate, String color,
        PetSize size, FurType furType, String ownerName, boolean active) {
}
