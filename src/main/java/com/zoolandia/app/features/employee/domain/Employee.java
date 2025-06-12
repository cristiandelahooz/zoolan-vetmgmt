package com.zoolandia.app.features.employee.domain;

import com.zoolandia.app.features.user.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import lombok.*;
import lombok.experimental.SuperBuilder;

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
  @PositiveOrZero(message = "Salary must be zero or positive")
  @Builder.Default
  private Double salary = 0.0;

  @Column(name = "hire_date")
  @NotNull(message = "Hire date is required")
  @Builder.Default
  private LocalDate hireDate = LocalDate.now();

  @Column(name = "available")
  @Builder.Default
  private boolean available = false;

  @Column(name = "work_schedule")
  @NotBlank(message = "Work schedule is required")
  private String workSchedule;

  @Column(name = "emergency_contact_name")
  @NotBlank(message = "Emergency contact name is required")
  private String emergencyContactName;

  @Column(name = "emergency_contact_phone")
  @NotBlank(message = "Emergency contact phone is required")
  private String emergencyContactPhone;
}
