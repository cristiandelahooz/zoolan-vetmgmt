package com.wornux.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

/**
 * Entity representing a veterinary consultation
 */
@Entity
@Table(name = "consultations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"createdAt", "updatedAt"})
@ToString
@Audited(withModifiedFlag = true)
public class Consultation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String notes;

  @Column(columnDefinition = "TEXT")
  private String diagnosis;

  @Column(columnDefinition = "TEXT")
  private String treatment;

  @Column(columnDefinition = "TEXT")
  private String prescription;

  @Column(name = "consultation_date", nullable = false)
  private LocalDateTime consultationDate;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "pet", nullable = false)
  private Pet pet;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "veterinarian", nullable = false)
  private Employee veterinarian;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "medical_history")
  private MedicalHistory medicalHistory;

  @Builder.Default
  @Column(nullable = false)
  private Boolean active = true;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  public boolean isActive() {
    return active != null && active;
  }
}