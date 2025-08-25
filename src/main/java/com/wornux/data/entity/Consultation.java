package com.wornux.data.entity;

import com.wornux.data.enums.ConsultationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.envers.Audited;

/** Entity representing a veterinary consultation */
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

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Builder.Default
  private ConsultationStatus status = ConsultationStatus.PENDIENTE;

  @Column(name = "assigned_at")
  private LocalDateTime assignedAt;

  @Column(name = "started_at")
  private LocalDateTime startedAt;

  @Column(name = "finished_at")
  private LocalDateTime finishedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "waiting_room_id")
  private WaitingRoom waitingRoom;

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
