package com.wornux.data.entity;

import static com.wornux.constants.AppointmentConstants.*;
import static com.wornux.constants.ValidationConstants.DOMINICAN_PHONE_PATTERN;

import com.wornux.data.enums.PetType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentClientInfo {

  @Column(name = "guest_client_name")
  @Size(
      max = MAX_GUEST_CLIENT_NAME_LENGTH,
      message = "El nombre del cliente no puede exceder {max} caracteres")
  @NotNull
  private String name;

  @Column(name = "guest_client_phone")
  @Pattern(
      regexp = DOMINICAN_PHONE_PATTERN,
      message = "Proporcione un número de teléfono válido (809, 849 o 829 seguido de 7 dígitos)")
  @Size(
      max = MAX_GUEST_CLIENT_PHONE_LENGTH,
      message = "El teléfono no puede exceder {max} caracteres")
  @NotNull
  private String phone;

  @Column(name = "guest_client_pet_type")
  @Enumerated(EnumType.STRING)
  @NotNull
  private PetType petType;

  @NotNull private String breed;

  @Column(name = "guest_client_email")
  @Email(message = "Proporcione un email válido")
  @Size(max = MAX_GUEST_CLIENT_EMAIL_LENGTH, message = "El email no puede exceder {max} caracteres")
  @NotNull
  private String email;
}
