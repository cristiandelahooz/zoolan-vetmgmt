package com.wornux.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateConsultationRequestDto {

    private String notes;
    private String diagnosis;
    private String treatment;
    private String prescription;
    private Long petId;
    private Long veterinarianId;
    private String consultationDate;
    private boolean active;
}
