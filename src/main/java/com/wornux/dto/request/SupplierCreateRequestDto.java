package com.wornux.dto.request;

import static com.wornux.constants.ValidationConstants.DOMINICAN_PHONE_PATTERN;
import static com.wornux.constants.ValidationConstants.RNC_PATTERN;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SupplierCreateRequestDto {

  @Pattern(
      regexp = RNC_PATTERN,
      message = "El RNC debe contener exactamente 9 dígitos o  11 dígitos")
  private String rnc;

  @NotBlank(message = "El nombre de la empresa es obligatorio")
  private String companyName;

  private String contactPerson;

  @Pattern(
      regexp = DOMINICAN_PHONE_PATTERN,
      message = "Proporcione un número de teléfono válido (809, 849 o 829 seguido de 7 dígitos)")
  private String contactPhone;

  @Email(message = "Por favor, proporcione una dirección de correo electrónico válida")
  private String contactEmail;

  @NotNull(message = "La provincia del suplidor es requerida")
  private String province;

  @NotNull(message = "El municipio del suplidor es requerido")
  private String municipality;

  @NotNull(message = "El sector del suplidor es requerido")
  private String sector;

  @NotNull(message = "La dirección de la calle del suplidor es requerida")
  private String streetAddress;
}
