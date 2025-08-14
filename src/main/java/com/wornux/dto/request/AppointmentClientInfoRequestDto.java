package com.wornux.dto.request;

import static com.wornux.constants.AppointmentConstants.*;
import static com.wornux.constants.ValidationConstants.DOMINICAN_PHONE_PATTERN;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
public class AppointmentClientInfoRequestDto {

  @Size(
      max = MAX_GUEST_CLIENT_NAME_LENGTH,
      message = "El nombre del cliente no puede exceder {max} caracteres")
  @Nullable
  private String name;

  @Pattern(
      regexp = DOMINICAN_PHONE_PATTERN,
      message = "Proporcione un número de teléfono válido (809, 849 o 829 seguido de 7 dígitos)")
  @Size(
      max = MAX_GUEST_CLIENT_PHONE_LENGTH,
      message = "El teléfono no puede exceder {max} caracteres")
  @Nullable
  private String phone;

  @Email(message = "Proporcione un email válido")
  @Size(max = MAX_GUEST_CLIENT_EMAIL_LENGTH, message = "El email no puede exceder {max} caracteres")
  @Nullable
  private String email;
}
