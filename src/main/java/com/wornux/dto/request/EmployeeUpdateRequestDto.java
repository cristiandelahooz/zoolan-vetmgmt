package com.wornux.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wornux.data.enums.EmployeeRole;
import com.wornux.data.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

import static com.wornux.constants.ValidationConstants.*;

@Data
@AllArgsConstructor
public class EmployeeUpdateRequestDto {

    @Size(min = MIN_USERNAME_LENGTH, max = MAX_USERNAME_LENGTH, message = "El usuario debe tener entre 3 y 50 caracteres")
    private String username;

    @Size(min = 2, message = "El nombre debe tener al menos 2 caracteres")
    private String firstName;

    @Size(min = 2, message = "El apellido debe tener al menos 2 caracteres")
    private String lastName;

    @Email(message = "Proporcione un correo electrónico válido")
    private String email;

    @Pattern(regexp = DOMINICAN_PHONE_PATTERN, message = "Proporcione un número de teléfono válido")
    private String phoneNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private Gender gender;

    private String nationality;

    @NotBlank(message = "La provincia es requerida")
    private String province;

    @NotBlank(message = "El municipio es requerido")
    private String municipality;

    @NotBlank(message = "El sector es requerido")
    private String sector;

    @NotBlank(message = "La dirección es requerida")
    private String streetAddress;


    @NotNull(message = "Employee role is required")
    private EmployeeRole employeeRole;

    @NotNull(message = "Salary is required")
    @DecimalMin(value = "0.0", message = "Salary must be greater than or equal to 0")
    private Double salary;

    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;

    @NotBlank(message = "Work schedule is required")
    private String workSchedule;

    @Nullable
    private String emergencyContactName;

    @Pattern(regexp = DOMINICAN_PHONE_PATTERN, message = "Proporcione un número de emergencia válido")
    @Nullable
    private String emergencyContactPhone;
}