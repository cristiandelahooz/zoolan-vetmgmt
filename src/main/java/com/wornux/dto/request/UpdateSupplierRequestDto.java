package com.wornux.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import static com.wornux.constants.ValidationConstants.DOMINICAN_PHONE_PATTERN;
import static com.wornux.constants.ValidationConstants.RNC_PATTERN;

@Data
public class UpdateSupplierRequestDto {

    private Long id;

    @Pattern(regexp = RNC_PATTERN, message = "El RNC debe contener exactamente 9 dígitos o  11 dígitos")
    private String rnc;

    private String companyName;

    private String contactPerson;

    @Pattern(regexp = DOMINICAN_PHONE_PATTERN, message = "Proporcione un número de teléfono válido (809, 849 o 829 seguido de 7 dígitos)")
    private String contactPhone;

    @Email(message = "Por favor, proporcione una dirección de correo electrónico válida")
    private String contactEmail;

    private String province;

    private String municipality;

    private String sector;

    private String streetAddress;

    private Boolean active;
}
