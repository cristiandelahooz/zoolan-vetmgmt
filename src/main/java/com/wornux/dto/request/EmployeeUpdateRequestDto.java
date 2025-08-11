package com.wornux.dto.request;

import com.wornux.data.enums.EmployeeRole;
import jakarta.validation.constraints.*;

import lombok.Data;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

import static com.wornux.constants.ValidationConstants.*;
import static com.wornux.constants.ValidationConstants.DOMINICAN_PHONE_PATTERN;

/**
 * DTO for updating an existing Employee All fields are nullable to allow partial updates
 */
@Data
public class EmployeeUpdateRequestDto {

    private Long id;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = MIN_USERNAME_LENGTH, max = MAX_USERNAME_LENGTH, message = "El nombre de usuario debe tener entre 3 y 20 caracteres")
    private String username;
    @NotBlank
    @Email(message = "El correo electrónico debe ser válido")
    private String email;
    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;
    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;
    @NotBlank(message = "El numero de teléfono es obligatorio")
    @Pattern(regexp = DOMINICAN_PHONE_PATTERN, message = "El número de teléfono debe ser un número válido de la República Dominicana")
    private String phoneNumber;
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private LocalDate birthDate;
    @NotNull(message = "El rol del empleado es obligatorio")
    private EmployeeRole employeeRole;

    private String nationality;
    private String province;
    private String municipality;
    @NotBlank(message = "El sector es obligatorio")
    private String sector;
    private String streetAddress;

    private String profilePicture;
    @NotNull(message = "El salario es obligatorio")
    @Positive(message = "El salario debe ser mayor que cero")
    private Double salary;
    @NotNull(message = "La fecha de contratación es obligatoria")
    private LocalDate hireDate;
    @NotBlank(message = "El horario de trabajo es obligatorio")
    private String workSchedule;
    @Nullable
    private String emergencyContactName;
    @Nullable
    @Pattern(regexp = DOMINICAN_PHONE_PATTERN, message = "El número de teléfono de emergencia debe ser un número válido de la República Dominicana")
    private String emergencyContactPhone;

    private boolean available;
    private boolean active;
}