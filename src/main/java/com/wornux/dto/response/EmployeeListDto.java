package com.wornux.dto.response;

import com.wornux.data.enums.EmployeeRole;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

@Data
public class EmployeeListDto {
    private Long id;
    private String username;
    @Nullable
    private String email;
    private String firstName;
    private String lastName;
    @Nullable
    private String phoneNumber;
    @Nullable
    private LocalDate birthDate;
    private EmployeeRole employeeRole;
    @Nullable
    private String nationality;
    private String province;
    private String municipality;
    private String streetAddress;
    @Nullable
    private String profilePicture;
    private Double salary;
    private LocalDate hireDate;
    private String workSchedule;
    @Nullable
    private String emergencyContactName;
    @Nullable
    private String emergencyContactPhone;
    private boolean available;
    private boolean active;
}