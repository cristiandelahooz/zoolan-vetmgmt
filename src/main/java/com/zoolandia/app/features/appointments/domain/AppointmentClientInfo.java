package com.zoolandia.app.features.appointments.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import static com.zoolandia.app.common.constants.AppointmentConstants.*;
import static com.zoolandia.app.common.constants.ValidationConstants.DOMINICAN_PHONE_PATTERN;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentClientInfo {
    
    @Column(name = "guest_client_name")
    @Size(max = MAX_GUEST_CLIENT_NAME_LENGTH, message = "El nombre del cliente no puede exceder {max} caracteres")
    @Nullable
    private String name;
    
    @Column(name = "guest_client_phone")
    @Pattern(regexp = DOMINICAN_PHONE_PATTERN, message = "Proporcione un número de teléfono válido (809, 849 o 829 seguido de 7 dígitos)")
    @Size(max = MAX_GUEST_CLIENT_PHONE_LENGTH, message = "El teléfono no puede exceder {max} caracteres")
    @Nullable
    private String phone;
    
    @Column(name = "guest_client_email")
    @Email(message = "Proporcione un email válido")
    @Size(max = MAX_GUEST_CLIENT_EMAIL_LENGTH, message = "El email no puede exceder {max} caracteres")
    @Nullable
    private String email;
}