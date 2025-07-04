package com.wornux.features.medicalHistory.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wornux.features.consultation.domain.Consultation;
import com.zoolandia.app.features.pet.domain.Pet;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medical_histories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"consultations", "pet"})
@ToString(exclude = {"consultations", "pet"})
public class MedicalHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "pet_id", unique = true)
    @JsonIgnore
    private Pet pet;

    @OneToMany(mappedBy = "medicalHistory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Consultation> consultations = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column(columnDefinition = "TEXT")
    private String medications;

    @Column(columnDefinition = "TEXT")
    private String vaccinations;

    @Column(columnDefinition = "TEXT")
    private String surgeries;

    @Column(columnDefinition = "TEXT")
    private String chronicConditions;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private boolean active = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addConsultation(Consultation consultation) {
        consultations.add(consultation);
        consultation.setMedicalHistory(this);
        updateLastModified();
    }

    private void updateLastModified() {
        this.updatedAt = LocalDateTime.now();
    }
}