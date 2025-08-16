package com.wornux.data.entity;

import static com.wornux.constants.ValidationConstants.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wornux.data.enums.Gender;
import com.wornux.data.enums.SystemRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;
import org.jspecify.annotations.Nullable;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"username"}),
    @UniqueConstraint(columnNames = {"email"})
})
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Audited(withModifiedFlag = true)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  protected Long id;

  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
  @Column(name = "username")
  protected String username;

  @Size(min = 8, message = "Password must be at least 8 characters long")
  @Column(name = "password")
  @JsonIgnore
  protected String password;

  @Email(message = "Please provide a valid email address")
  @Column(name = "email")
  @Nullable
  protected String email;

  @Column(name = "first_name")
  protected String firstName;

  @Column(name = "last_name")
  protected String lastName;

  @Pattern(
      regexp = DOMINICAN_PHONE_PATTERN,
      message = "Proporcione un número de teléfono válido (809, 849 o 829 seguido de 7 dígitos)")
  @Column(name = "phone_number")
  @Nullable
  protected String phoneNumber;

  @Column(name = "birth_date")
  @Nullable
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN)
  protected LocalDate birthDate;

  @Column(name = "gender")
  @Enumerated(EnumType.STRING)
  protected Gender gender;

  @Column(name = "nationality")
  @Nullable
  protected String nationality;

  @Column(name = "province")
  @NotNull(message = "La provincia es requerida")
  protected String province;

  @Column(name = "municipality")
  @NotNull(message = "El municipio es requerido")
  protected String municipality;

  @Column(name = "sector")
  @NotNull(message = "El sector es requerido")
  protected String sector;

  @Column(name = "street_address")
  @NotNull(message = "La dirección de la calle es requerida")
  protected String streetAddress;

  @Column(name = "reference_points", length = 500)
  @Nullable
  protected String referencePoints;

  @Column(name = "active")
  @Builder.Default
  protected boolean active = true;

  @Column(name = "created_at", updatable = false)
  protected LocalDateTime createdAt;

  @Column(name = "updated_at")
  protected LocalDateTime updatedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "system_role")
  protected SystemRole systemRole;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
