package com.wornux.data.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.wornux.constants.ValidationConstants.DOMINICAN_PHONE_PATTERN;
import static com.wornux.constants.ValidationConstants.RNC_PATTERN;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "suppliers")
@Audited(withModifiedFlag = true)
public class Supplier {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "supplier_id")
  protected Long id;

  @Pattern(regexp = RNC_PATTERN, message = "El RNC debe contener exactamente 9 dígitos")
  @Column(name = "rnc", length = 11, nullable = false, unique = true, columnDefinition = "CHAR(11)")
  private String rnc;

  @Column(name = "company_name", nullable = false)
  private String companyName;

  @Column(name = "contact_person")
  @Nullable
  private String contactPerson;

  @Pattern(
      regexp = DOMINICAN_PHONE_PATTERN,
      message = "Proporcione un número de teléfono válido (809, 849 o 829 seguido de 7 dígitos)")
  @Column(name = "contact_phone")
  @Nullable
  private String contactPhone;

  @Email(message = "Por favor, proporcione una dirección de correo electrónico válida")
  @Column(name = "contact_email", unique = true)
  @Nullable
  protected String contactEmail;

  @Column(name = "province")
  @NotNull(message = "La provincia del suplidor es requerida")
  protected String province;

  @Column(name = "municipality")
  @NotNull(message = "El municipio del suplidor es requerido")
  protected String municipality;

  @Column(name = "sector")
  @NotNull(message = "El sector del suplidor es requerido")
  protected String sector;

  @Column(name = "street_address")
  @NotNull(message = "La dirección de la calle del suplidor es requerido")
  protected String streetAddress;

  @Column(name = "active")
  @Builder.Default
  private boolean active = true;

  @OneToMany(mappedBy = "supplier", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @JsonBackReference
  @Builder.Default
  private List<Product> products = new ArrayList<>();
}
