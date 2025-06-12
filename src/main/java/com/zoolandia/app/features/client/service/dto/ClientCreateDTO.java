package com.zoolandia.app.features.client.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zoolandia.app.features.client.domain.ClientRating;
import com.zoolandia.app.features.client.domain.PreferredContactMethod;
import com.zoolandia.app.features.client.domain.ReferenceSource;
import com.zoolandia.app.features.user.domain.Gender;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ClientCreateDTO {
  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
  private String username;

  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Password must be at least 8 characters long")
  private String password;

  @Email(message = "Please provide a valid email address")
  private String email;

  @NotBlank(message = "First name is required")
  private String firstName;

  @NotBlank(message = "Last name is required")
  private String lastName;

  @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Please provide a valid phone number")
  private String phoneNumber;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate birthDate;

  @NotNull private Gender gender;

  @NotBlank(message = "La nacionalidad es requerida")
  private String nationality;

  @Pattern(regexp = "^[0-9]{11}$", message = "La cédula debe contener exactamente 11 dígitos")
  private String cedula;

  @Pattern(
      regexp = "^[0-9A-Z]{9}$",
      message = "El pasaporte debe contener 9 caracteres alfanuméricos")
  private String passport;

  @Pattern(regexp = "^[0-9]{9}$", message = "El RNC debe contener exactamente 9 dígitos")
  private String rnc;

  private String companyName;

  @NotNull(message = "El método de contacto preferido es requerido")
  private PreferredContactMethod preferredContactMethod;

  private String emergencyContactName;

  @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Proporcione un número de emergencia válido")
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
