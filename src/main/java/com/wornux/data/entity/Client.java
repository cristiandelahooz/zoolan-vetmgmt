package com.wornux.data.entity;

import static com.wornux.constants.ValidationConstants.*;

import com.wornux.data.enums.ClientRating;
import com.wornux.data.enums.PreferredContactMethod;
import com.wornux.data.enums.ReferenceSource;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.envers.Audited;
import org.jspecify.annotations.Nullable;

import static com.wornux.constants.ValidationConstants.*;

import com.wornux.data.enums.PreferredContactMethod;
import com.wornux.data.enums.ClientRating;
import com.wornux.data.enums.ReferenceSource;

@Entity
@Table(name = "client", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"cedula"}),
    @UniqueConstraint(columnNames = {"passport"}),
    @UniqueConstraint(columnNames = {"rnc"})
})
@PrimaryKeyJoinColumn(name = "client_id")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Audited(withModifiedFlag = true)
public class Client extends User {

  @Pattern(regexp = CEDULA_PATTERN, message = "La cédula debe contener exactamente 11 dígitos")
  @Column(
      name = "cedula",
      length = 11,
      columnDefinition = "CHAR(11)")
  @Nullable
  private String cedula;

  @Pattern(
      regexp = PASSPORT_PATTERN,
      message = "El pasaporte debe contener 9 caracteres alfanuméricos")
  @Column(
      name = "passport",
      length = 9,
      columnDefinition = "CHAR(9)")
  @Nullable
  private String passport;

    @Pattern(regexp = RNC_PATTERN, message = "El RNC debe contener exactamente 9 dígitos")
    @Column(name = "rnc", length = 11, nullable = true, unique = true, columnDefinition = "CHAR(11)")
    @Nullable
    private String rnc;

  @Column(name = "company_name")
  @Nullable
  private String companyName;

  @Column(name = "preferred_contact_method")
  @Enumerated(EnumType.STRING)
  private PreferredContactMethod preferredContactMethod;

  @Column(name = "emergency_contact_name")
  @Nullable
  private String emergencyContactName;

  @Pattern(
      regexp = DOMINICAN_PHONE_PATTERN_OPTIONAL,
      message =
          "Proporcione un número de teléfono de emergencia válido (809, 849 o 829 seguido de 7"
              + " dígitos)")
  @Column(name = "emergency_contact_number")
  @Nullable
  private String emergencyContactNumber;

  @Column(name = "rating")
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private ClientRating rating = ClientRating.BUENO;

  @Column(name = "credit_limit")
  @Builder.Default
  private Double creditLimit = 0.0;

  @Column(name = "current_balance")
  @Builder.Default
  private Double currentBalance = 0.0;

  @Column(name = "payment_terms_days")
  @Builder.Default
  private Integer paymentTermsDays = 0;

  @Column(name = "notes", length = 500)
  @ColumnTransformer(write = "UPPER(?)")
  @Nullable
  private String notes;

  @Column(name = "reference_source")
  @Enumerated(EnumType.STRING)
  @Nullable
  private ReferenceSource referenceSource;

  @Column(name = "verified")
  @Builder.Default
  private boolean verified = false;

  @AssertTrue(
      message = "Debe proporcionar exactamente uno de los siguientes: cédula, pasaporte o RNC")
  private boolean isValidIdentification() {
    boolean hasCedula = cedula != null && !cedula.trim().isEmpty();
    boolean hasPassport = passport != null && !passport.trim().isEmpty();
    boolean hasRnc = rnc != null && !rnc.trim().isEmpty();

    int filledFields = 0;
    if (hasCedula) {
      filledFields++;
    }
    if (hasPassport) {
      filledFields++;
    }
    if (hasRnc) {
      filledFields++;
    }

    return filledFields == MAX_IDENTIFICATION_DOCUMENT_COUNT;
  }

  public String getFullName() {
    if (firstName == null || lastName == null) {
      return companyName != null ? companyName : "Cliente Sin Nombre";
    }
    return String.format("%s %s", firstName, lastName);
  }
}
