package com.zoolandia.app.features.employee.domain;

import com.zoolandia.app.features.user.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "employee")
@PrimaryKeyJoinColumn(name = "employee_id")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Employee extends User {

    @Column(name = "employee_role")
    @Enumerated(EnumType.STRING)
    private EmployeeRole employeeRole;

    @Column(name = "salary")
    @Builder.Default
    private Double salary = 0.0;

    @Column(name = "hire_date")
    @NotNull(message = "Hire date is required")
    @Builder.Default
    private LocalDate hireDate = LocalDate.now();
    
    @Column(name = "available")
    @Builder.Default
    private boolean available = false;

}