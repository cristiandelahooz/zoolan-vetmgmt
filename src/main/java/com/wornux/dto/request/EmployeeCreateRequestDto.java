package com.wornux.dto.request;

import com.wornux.data.enums.EmployeeRole;
import com.wornux.data.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for creating a new Employee Includes all required fields from both User and Employee entities
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreateRequestDto {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Please provide a valid email address")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Please provide a valid phone number")
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

    private String profilePictureUrl;

    @NotNull(message = "Employee role is required")
    private EmployeeRole employeeRole;

    @NotNull(message = "Salary is required")
    @DecimalMin(value = "0.0", message = "Salary must be greater than or equal to 0")
    private Double salary;

    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;

    @Builder.Default
    private boolean available = false;

    @Builder.Default
    private boolean active = true;

    @NotBlank(message = "Work schedule is required")
    private String workSchedule;

    @NotBlank(message = "Emergency contact name is required")
    private String emergencyContactName;

    @NotBlank(message = "Emergency contact phone is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Please provide a valid emergency contact phone number")
    private String emergencyContactPhone;

}