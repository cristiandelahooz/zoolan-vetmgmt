package com.zoolandia.app.features.appointments.domain;

import com.zoolandia.app.features.client.domain.Client;
import com.zoolandia.app.features.employee.domain.Employee;
import com.zoolandia.app.features.pet.domain.Pet;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

import static com.zoolandia.app.common.constants.AppointmentConstants.*;

@Entity
@Table(name = "appointments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_date", nullable = false)
    @NotNull(message = "La fecha y hora de la cita es obligatoria")
    @Future(message = "La fecha de la cita debe ser en el futuro")
    private LocalDateTime appointmentDateTime;

    @Column(name = "duration_minutes", nullable = false)
    @Min(value = MIN_APPOINTMENT_DURATION_MINUTES, message = "La duración mínima es {value} minutos")
    @Max(value = MAX_APPOINTMENT_DURATION_MINUTES, message = "La duración máxima es {value} minutos")
    @Builder.Default
    private Integer durationMinutes = DEFAULT_APPOINTMENT_DURATION_MINUTES;

    @Column(name = "service_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "El tipo de servicio es obligatorio")
    private ServiceType serviceType;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.PROGRAMADA;

    @Column(name = "reason", length = MAX_REASON_LENGTH)
    @Size(max = MAX_REASON_LENGTH, message = "El motivo no puede exceder {max} caracteres")
    @Nullable
    private String reason;

    @Column(name = "notes", length = MAX_APPOINTMENT_NOTES_LENGTH)
    @Size(max = MAX_APPOINTMENT_NOTES_LENGTH, message = "Las notas no pueden exceder {max} caracteres")
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

    @Embedded
    @Valid
    @Nullable
    private AppointmentClientInfo guestClientInfo;

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

    public LocalDateTime getEndDateTime() {
        return appointmentDateTime.plusMinutes(durationMinutes);
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
        return serviceType.isClinical();
    }

    public String getClientDisplayName() {
        if (hasRegisteredClient()) {
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

    @AssertTrue(message = "Debe proporcionar información de cliente registrado o datos de cliente invitado")
    private boolean isValidClientInfo() {
        boolean hasRegisteredClient = client != null;
        boolean hasGuestInfo = guestClientInfo != null &&
                guestClientInfo.getName() != null &&
                !guestClientInfo.getName().trim().isEmpty();

        return hasRegisteredClient || hasGuestInfo;
    }

    @AssertTrue(message = "Si se especifica una mascota, debe tener un cliente registrado asociado")
    private boolean isValidPetClientRelation() {
        if (pet != null) {
            return client != null && client.equals(pet.getOwner());
        }
        return true;
    }
}