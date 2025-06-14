package com.zoolandia.app.features.client.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zoolandia.app.features.client.domain.ClientRating;
import com.zoolandia.app.features.client.domain.ClientValidationGroup;
import com.zoolandia.app.features.client.domain.PreferredContactMethod;
import com.zoolandia.app.features.client.domain.ReferenceSource;
import com.zoolandia.app.features.user.domain.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ClientCreateDTO {
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Pattern(regexp = "^(809|849|829)\\d{7}$", message = "Proporcione un número de teléfono válido (809, 849 o 829 seguido de 7 dígitos)")
    private String phoneNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotNull
    private Gender gender;

    @NotBlank(message = "La nacionalidad es requerida")
    private String nationality;

    @Pattern(regexp = "\\d{11}$", message = "La cédula debe contener exactamente 11 dígitos")
    private String cedula;

    @Pattern(regexp = "^[0-9A-Z]{9}$", message = "El pasaporte debe contener 9 caracteres alfanuméricos")
    private String passport;

    @Pattern(regexp = "\\d{9}$", message = "El RNC debe contener exactamente 9 dígitos")
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

    @AssertTrue(message = "Debe proporcionar al menos cédula, pasaporte o RNC")
    private boolean isValidIdentification() {
        return (cedula != null && !cedula.trim().isEmpty()) ||
                (passport != null && !passport.trim().isEmpty()) ||
                (rnc != null && !rnc.trim().isEmpty());
    }
}