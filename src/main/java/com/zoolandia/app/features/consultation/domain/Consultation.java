package com.zoolandia.app.features.consultation.domain;

import com.zoolandia.app.features.employee.domain.Employee;
import com.zoolandia.app.features.medicalHistory.domain.MedicalHistory;
import com.zoolandia.app.features.pet.domain.Pet;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;


import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "consultation")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Consultation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consultation_id")
    private Long id;

    @Column(name = "notes")
    @NotNull
    @Lob
    private String notes;

    @Column(name = "diagnosis")
    @NotNull
    @Lob
    private String diagnosis;

    @Column(name = "treatment")
    @Nullable
    private String treatment;

    @Column(name = "prescription")
    @Nullable
    private String prescription;

    @Column(name = "consultation_date")
    @NotNull(message = "Consultation date is required")
    private LocalDateTime consultationDate;

    @ManyToOne
    @JoinColumn(name = "pet_id")
    @NotNull(message = "Pet is required")
    private Pet pet;

    @ManyToOne
    @JoinColumn(name = "veterinarian_id")
    @NotNull(message = "Veterinarian is required")
    private Employee veterinarian;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "medical_history_id")
    private MedicalHistory medicalHistory;


    public static class AuditListener {
        @PrePersist
        public void prePersist(Consultation c) {
            LocalDateTime now = LocalDateTime.now();
            c.createdAt = now;
            c.updatedAt = now;
        }

        @PreUpdate
        public void preUpdate(Consultation c) {
            c.updatedAt = LocalDateTime.now();
        }
    }
}
