package com.wornux.data.entity;

import static com.wornux.constants.AppointmentConstants.MAX_APPOINTMENT_NOTES_LENGTH;
import static com.wornux.constants.AppointmentConstants.MAX_REASON_LENGTH;

import com.wornux.data.enums.AppointmentStatus;
import com.wornux.data.enums.OfferingType;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "appointments")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Audited(withModifiedFlag = true)
public class Appointment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "start_appointment_date", nullable = false)
  @NotNull(message = "La fecha y hora de inicio es obligatoria")
  @Future(message = "La fecha de inicio de la cita debe ser en el futuro")
  private LocalDateTime startAppointmentDate;

  @Column(name = "end_appointment_date", nullable = false)
  @NotNull(message = "La fecha y hora de cierre es obligatoria")
  @Future(message = "La fecha de cierre de la cita debe ser en el futuro")
  private LocalDateTime endAppointmentDate;

  @Column(name = "offering_type", nullable = false)
  @Enumerated(EnumType.STRING)
  @NotNull(message = "El tipo de servicio es obligatorio")
  private OfferingType offeringType;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private AppointmentStatus status = AppointmentStatus.PROGRAMADA;

  @Column(name = "reason", length = MAX_REASON_LENGTH)
  @Size(max = MAX_REASON_LENGTH, message = "El motivo no puede exceder {max} caracteres")
  @Nullable
  private String reason;

  @Column(name = "notes", length = MAX_APPOINTMENT_NOTES_LENGTH)
  @Size(
      max = MAX_APPOINTMENT_NOTES_LENGTH,
      message = "Las notas no pueden exceder {max} caracteres")
  @Nullable
  private String notes;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_id")
  @Nullable
  private Client client;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pet_id")
  @Nullable
  private Pet pet;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_id")
  @Nullable
  private Employee assignedEmployee;

  @Embedded @Valid @Nullable private AppointmentClientInfo guestClientInfo;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "created_by")
  @Nullable
  private String createdBy;

  @Column(name = "updated_by")
  @Nullable
  private String updatedBy;

  public String getAppointmentTitle() {
    return getClientDisplayName() + " - " + offeringType.getDisplay();
  }

  public boolean isCompleted() {
    return status == AppointmentStatus.COMPLETADA;
  }

  public boolean isCancelled() {
    return status == AppointmentStatus.CANCELADA;
  }

  public boolean hasRegisteredClient() {
    return client != null;
  }

  public boolean requiresVeterinarian() {
    return offeringType.getDescription().equals("MEDICAL");
  }

  public String getClientDisplayName() {
    if (client != null) {
      return client.getFirstName() + " " + client.getLastName();
    } else if (guestClientInfo != null && guestClientInfo.getName() != null) {
      return guestClientInfo.getName();
    }
    return "Cliente sin especificar";
  }

  public String getClientContactPhone() {
    if (hasRegisteredClient()) {
      return client.getPhoneNumber();
    } else if (guestClientInfo != null) {
      return guestClientInfo.getPhone();
    }
    return null;
  }

  public String getEmployeeDisplayName() {
    if (assignedEmployee != null) {
      return assignedEmployee.getFirstName() + " " + assignedEmployee.getLastName();
    }
    return "Empleado no asignado";
  }

  @AssertTrue(
      message = "Debe proporcionar información de cliente registrado o datos de cliente invitado")
  private boolean isValidClientInfo() {
    boolean hasRegisteredClient = client != null;
    boolean hasGuestInfo =
        guestClientInfo != null
            && guestClientInfo.getName() != null
            && !guestClientInfo.getName().trim().isEmpty();

    log.info(
        "Validating client info: hasRegisteredClient={}, hasGuestInfo={}",
        hasRegisteredClient,
        hasGuestInfo);
    return hasRegisteredClient || hasGuestInfo;
  }

  @AssertTrue(message = "Si se especifica una mascota, debe tener un cliente registrado asociado")
  private boolean isValidPetClientRelation() {
    if (pet != null) {
      return client != null && pet.getOwners().contains(client);
    }
    return true;
  }
}
