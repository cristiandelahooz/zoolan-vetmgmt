package com.zoolandia.app.features.employee.service.dto;

import com.zoolandia.app.features.employee.domain.EmployeeRole;
import com.zoolandia.app.features.user.domain.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for updating an existing Employee
 * All fields are nullable to allow partial updates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUpdateDTO {
    
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    private String firstName;
    private String lastName;

    @Email(message = "Please provide a valid email address")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Please provide a valid phone number")
    private String phoneNumber;

    private LocalDate birthDate;
    private Gender gender;
    private String nationality;

    private String province;
    private String municipality;
    private String sector;
    private String streetAddress;

    private String profilePictureUrl;

    private EmployeeRole employeeRole;

    @DecimalMin(value = "0.0", message = "Salary must be greater than or equal to 0")
    private Double salary;

    private LocalDate hireDate;

    private Boolean active;

    private String specializations;
    private String certifications;
    private Boolean availableForEmergencies;
    private String workSchedule;
    
    @Size(max = 1000)
    private String notes;

    private Boolean available;



}