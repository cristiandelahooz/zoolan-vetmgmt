package com.wornux.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "consultations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "medicalHistory" })
@ToString(exclude = { "medicalHistory" })
@Audited(withModifiedFlag = true)
public class Consultation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String treatment;

    @Column(columnDefinition = "TEXT")
    private String prescription;

    @Column(nullable = false)
    private LocalDateTime consultationDate;

    @ManyToOne
    @JoinColumn(name = "pet", nullable = false)
    private Pet pet;

    @ManyToOne
    @JoinColumn(name = "veterinarian", nullable = false)
    private Employee veterinarian;

    @ManyToOne
    @JoinColumn(name = "medical_history")
    @JsonIgnore
    @Setter
    private MedicalHistory medicalHistory;

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
}
