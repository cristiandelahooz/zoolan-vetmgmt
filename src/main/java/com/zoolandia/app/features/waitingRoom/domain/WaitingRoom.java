package com.zoolandia.app.features.waitingRoom.domain;

import com.zoolandia.app.features.client.domain.Client;
import com.zoolandia.app.features.pet.domain.Pet;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "waiting_room")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WaitingRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @NotNull
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    @NotNull
    private Pet pet;

    @Column(name = "arrival_time", nullable = false)
    @NotNull
    private LocalDateTime arrivalTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull
    private WaitingRoomStatus status = WaitingRoomStatus.WAITING;

    @Column(name = "reason_for_visit")
    private String reasonForVisit;

    @Column(name = "priority", nullable = false)
    private Integer priority = 1; // 1 = Normal, 2 = Urgent, 3 = Emergency

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

    // Método para marcar como en consulta
    public void startConsultation() {
        this.status = WaitingRoomStatus.IN_CONSULTATION;
        this.consultationStartedAt = LocalDateTime.now();
    }

    // Método para completar la consulta
    public void completeConsultation() {
        this.status = WaitingRoomStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}
