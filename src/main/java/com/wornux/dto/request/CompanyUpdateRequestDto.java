package com.wornux.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wornux.data.enums.ClientRating;
import com.wornux.data.enums.PreferredContactMethod;
import com.wornux.data.enums.ReferenceSource;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

import static com.wornux.constants.ValidationConstants.*;

@Data
public class CompanyUpdateRequestDto {
    @Size(min = 2, message = "El nombre de la empresa debe tener al menos 2 caracteres")
    private String companyName;

    @Pattern(regexp = RNC_PATTERN_OPTIONAL, message = "El RNC debe contener exactamente 9 dígitos")
    private String rnc;

    @Email(message = "Proporcione una dirección de correo electrónico válida")
    private String email;

    @Pattern(regexp = DOMINICAN_PHONE_PATTERN, message = "Proporcione un número de teléfono válido")
    private String phoneNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate incorporationDate;

    private String nationality;

    private ClientRating rating;

    private PreferredContactMethod preferredContactMethod;

    private String emergencyContactName;

    @Pattern(regexp = DOMINICAN_PHONE_PATTERN_OPTIONAL, message = "Proporcione un número de emergencia válido")
    private String emergencyContactNumber;

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