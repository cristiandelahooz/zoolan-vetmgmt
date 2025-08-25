package com.wornux.data.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.envers.Audited;

/** Entity representing a grooming (esthetic) session */
@Entity
@Table(name = "grooming_sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"createdAt", "updatedAt"})
@ToString
@Audited(withModifiedFlag = true)
public class GroomingSession {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String notes;

  @Column(name = "grooming_date", nullable = false)
  private LocalDateTime groomingDate;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "pet", nullable = false)
  private Pet pet;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "groomer", nullable = false)
  private Employee groomer;

  @Builder.Default
  @Column(nullable = false)
  private Boolean active = true;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "waiting_room_id")
  private WaitingRoom waitingRoom;


  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (groomingDate == null) {
      groomingDate = LocalDateTime.now();
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  public boolean isActive() {
    return active != null && active;
  }
}
