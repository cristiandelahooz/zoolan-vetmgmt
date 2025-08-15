package com.wornux.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wornux.data.enums.ClientRating;
import com.wornux.data.enums.PreferredContactMethod;
import com.wornux.data.enums.ReferenceSource;
import com.wornux.data.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;
import static com.wornux.constants.ValidationConstants.*;


import java.time.LocalDate;

import lombok.AllArgsConstructor;


@Data
@AllArgsConstructor
public class ClientUpdateRequestDto {
  @Email(message = "Please provide a valid email address")
  private String email;

  @Size(min = 2, message = "First name must be at least 2 characters")
  private String firstName;

  @Size(min = 2, message = "Last name must be at least 2 characters")
  private String lastName;

  @Pattern(regexp = DOMINICAN_PHONE_PATTERN, message = "Please provide a valid phone number")
  private String phoneNumber;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate birthDate;

  private Gender gender;

  private String nationality;

  @Pattern(regexp = CEDULA_PATTERN, message = "La cédula debe contener exactamente 11 dígitos")
  private String cedula;

  @Pattern(
      regexp = PASSPORT_PATTERN,
      message = "El pasaporte debe contener 9 caracteres alfanuméricos")
  private String passport;

    @Pattern(regexp = RNC_PATTERN, message = "El RNC debe contener exactamente o 11 dígitos")
    private String rnc;

  private String companyName;

  private PreferredContactMethod preferredContactMethod;

  private String emergencyContactName;

  @Pattern(regexp = DOMINICAN_PHONE_PATTERN_OPTIONAL, message = "Proporcione un número de emergencia válido")
  private String emergencyContactNumber;

  private ClientRating rating;

  @PositiveOrZero(message = "El límite de crédito no puede ser negativo")
  private Double creditLimit;

  @PositiveOrZero(message = "Los días de término de pago no pueden ser negativos")
  private Integer paymentTermsDays;

  @Size(max = 1000, message = "Las notas no pueden exceder 1000 caracteres")
  private String notes;

  private ReferenceSource referenceSource;

  @NotBlank(message = "La provincia es requerida")
  private String province;

  @NotBlank(message = "El municipio es requerido")
  private String municipality;

  @NotBlank(message = "El sector es requerido")
  private String sector;

  @NotBlank(message = "La dirección es requerida")
  private String streetAddress;

  @Size(max = 500, message = "Los puntos de referencia no pueden exceder 500 caracteres")
  private String referencePoints;
}