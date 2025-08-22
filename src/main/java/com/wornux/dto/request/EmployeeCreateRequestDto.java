package com.wornux.dto.request;

import static com.wornux.constants.ValidationConstants.DOMINICAN_PHONE_PATTERN;

import com.wornux.data.enums.EmployeeRole;
import com.wornux.data.enums.Gender;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * DTO for creating a new Employee Includes all required fields from both User and Employee entities
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreateRequestDto {

  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 50, message = "Nombre de usuario debe tener entre 3 y 50 caracteres")
  private String username;

  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Contraseña debe tener al menos 8 caracteres")
  private String password;

  @NotBlank(message = "Primer nombre es requerido")
  private String firstName;

  @NotBlank(message = "Apellido es requerido")
  private String lastName;

  @Email(message = "Por favor, proporciona un correo electrónico válido")
  private String email;

  @Pattern(regexp = DOMINICAN_PHONE_PATTERN, message = "Please provide a valid phone number")
  private String phoneNumber;

  private LocalDate birthDate;

  private Gender gender;

  private String nationality;

  @NotNull(message = "La provincia es requerida")
  private String province;

  @NotNull(message = "El municipio es requerido")
  private String municipality;

  @NotNull(message = "El sector es requerido")
  private String sector;

  @NotNull(message = "La dirección es requerida")
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

  @Nullable private String emergencyContactName;

  @Pattern(regexp = DOMINICAN_PHONE_PATTERN, message = "Proporcione un número de emergencia válido")
  @Nullable
  private String emergencyContactPhone;
}
