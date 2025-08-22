package com.wornux.data.entity;

import com.wornux.data.enums.Priority;
import com.wornux.data.enums.WaitingRoomStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.proxy.HibernateProxy;

@Entity
@Table(name = "waiting_room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Audited(withModifiedFlag = true)
public class WaitingRoom {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "client", nullable = false)
  @NotNull
  private Client client;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "pet", nullable = false)
  @NotNull
  private Pet pet;

  @Column(name = "arrival_time", nullable = false)
  @NotNull
  private LocalDateTime arrivalTime;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @NotNull
  private WaitingRoomStatus status = WaitingRoomStatus.ESPERANDO;

  @Column(name = "reason_for_visit")
  private String reasonForVisit;

  @Enumerated(EnumType.STRING)
  @Column(name = "priority", nullable = false)
  @NotNull
  private Priority priority = Priority.NORMAL;

  @Column(name = "notes")
  private String notes;

  @Column(name = "consultation_started_at")
  private LocalDateTime consultationStartedAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @PrePersist
  public void prePersist() {
    if (arrivalTime == null) {
      arrivalTime = LocalDateTime.now();
    }
  }

  public void startConsultation() {
    this.status = WaitingRoomStatus.EN_CONSULTA;
    this.consultationStartedAt = LocalDateTime.now();
  }

  public void completeConsultation() {
    this.status = WaitingRoomStatus.COMPLETADO;
    this.completedAt = LocalDateTime.now();
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass =
        o instanceof HibernateProxy
            ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();
    Class<?> thisEffectiveClass =
        this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    WaitingRoom that = (WaitingRoom) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy
        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
