package com.zoolandia.app.features.client.service.dto;

import static com.zoolandia.app.common.constants.ValidationConstants.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zoolandia.app.features.client.domain.ClientRating;
import com.zoolandia.app.features.client.domain.PreferredContactMethod;
import com.zoolandia.app.features.client.domain.ReferenceSource;
import com.zoolandia.app.features.user.domain.Gender;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

public record ClientCreateDTO(
    @Email(message = "Please provide a valid email address") String email,
    String firstName,
    String lastName,
    @Pattern(
            regexp = DOMINICAN_PHONE_PATTERN,
            message =
                "Proporcione un número de teléfono válido (809, 849 o 829 seguido de 7 dígitos)")
        String phoneNumber,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN)
        @Nullable LocalDate birthDate,
    @Nullable Gender gender,
    String nationality,
    @Pattern(regexp = CEDULA_PATTERN, message = "La cédula debe contener exactamente 11 dígitos")
        @Nullable String cedula,
    @Pattern(
            regexp = PASSPORT_PATTERN,
            message = "El pasaporte debe contener 9 caracteres alfanuméricos")
        @Nullable String passport,
    @Pattern(regexp = RNC_PATTERN, message = "El RNC debe contener exactamente 9 dígitos")
        @Nullable String rnc,
    @Nullable String companyName,
    PreferredContactMethod preferredContactMethod,
    @Nullable String emergencyContactName,
    @Pattern(
            regexp = DOMINICAN_PHONE_PATTERN,
            message = "Proporcione un número de emergencia válido")
        @Nullable String emergencyContactNumber,
    @Nullable ClientRating rating,
    @PositiveOrZero(message = "El límite de crédito no puede ser negativo")
        @Nullable Double creditLimit,
    @PositiveOrZero(message = "Los días de término de pago no pueden ser negativos")
        @Nullable Integer paymentTermsDays,
    @Size(max = 1000, message = "Las notas no pueden exceder 1000 caracteres")
        @Nullable String notes,
    @Nullable ReferenceSource referenceSource,
    @NotBlank(message = "La provincia es requerida") String province,
    @NotBlank(message = "El municipio es requerido") String municipality,
    @NotBlank(message = "El sector es requerido") String sector,
    @NotBlank(message = "La dirección es requerida") String streetAddress,
    @Size(max = 500, message = "Los puntos de referencia no pueden exceder 500 caracteres")
        @Nullable String referencePoints) {
  @AssertTrue(
      message = "Debe proporcionar exactamente uno de los siguientes: cédula, pasaporte o RNC")
  private boolean isValidIdentification() {
    return countProvidedIdentificationDocuments() == MAX_IDENTIFICATION_DOCUMENT_COUNT;
  }

  private int countProvidedIdentificationDocuments() {
    return (int) getIdentificationDocuments().filter(this::isDocumentProvided).count();
  }

  private Stream<String> getIdentificationDocuments() {
    return Stream.of(cedula, passport, rnc);
  }

  private boolean isDocumentProvided(String document) {
    return document != null && !isBlankString(document);
  }

  private boolean isBlankString(String value) {
    return value.trim().isEmpty();
  }
}
