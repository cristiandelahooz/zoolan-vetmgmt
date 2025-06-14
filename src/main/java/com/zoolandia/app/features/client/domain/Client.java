package com.zoolandia.app.features.client.domain;

import com.zoolandia.app.features.user.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnTransformer;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "client")
@PrimaryKeyJoinColumn(name = "client_id")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Client extends User {

    @Override
    @Column(name = "username", nullable = true)
    @Nullable
    public String getUsername() {
        return super.getUsername();
    }

    @Override
    public void setUsername(String username) {
        super.setUsername(username);
    }

    @Override
    @Column(name = "password", nullable = true)
    @Nullable
    public String getPassword() {
        return super.getPassword();
    }

    @Override
    public void setPassword(String password) {
        super.setPassword(password);
    }

    @Pattern(regexp = "^[0-9]{11}$", message = "La cédula debe contener exactamente 11 dígitos")
    @Column(name = "cedula", length = 11)
    @Nullable
    private String cedula;

    @Pattern(regexp = "^[0-9A-Z]{9}$", message = "El pasaporte debe contener 9 caracteres alfanuméricos")
    @Column(name = "passport", length = 9)
    @Nullable
    private String passport;

    @Pattern(regexp = "^[0-9]{9}$", message = "El RNC debe contener exactamente 9 dígitos")
    @Column(name = "rnc", length = 9)
    @Nullable
    private String rnc;

    @AssertTrue(message = "Debe proporcionar al menos cédula o pasaporte")
    private boolean isValidIdentification() {
        return cedula != null || passport != null;
    }

    @Column(name = "company_name")
    @Nullable
    private String companyName;

    @Column(name = "preferred_contact_method")
    @Enumerated(EnumType.STRING)
    private PreferredContactMethod preferredContactMethod;

    @Column(name = "emergency_contact_name")
    @Nullable
    private String emergencyContactName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Proporcione un número de teléfono válido")
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

    @Column(name = "notes", length = 1000)
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
}