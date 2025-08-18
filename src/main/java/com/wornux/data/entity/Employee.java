package com.wornux.data.entity;

import com.wornux.data.enums.EmployeeRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "employee")
@PrimaryKeyJoinColumn(name = "employee_id")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Audited(withModifiedFlag = true)
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
  private boolean available = true;

  @Column(name = "work_schedule")
  @NotBlank(message = "Work schedule is required")
  private String workSchedule;

  @Column(name = "emergency_contact_name")
  @Nullable
  private String emergencyContactName;

  @Column(name = "emergency_contact_phone")
  @Nullable
  private String emergencyContactPhone;
}
