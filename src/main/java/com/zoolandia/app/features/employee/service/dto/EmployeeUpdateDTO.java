package com.zoolandia.app.features.employee.service.dto;

import com.zoolandia.app.features.employee.domain.EmployeeRole;
import com.zoolandia.app.dto.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for updating an existing Employee All fields are nullable to allow partial updates
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

    private String email;

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

    private Double salary;

    private LocalDate hireDate;

    private boolean available;

    private boolean active;

    private String workSchedule;

    private String emergencyContactName;

    private String emergencyContactPhone;

}