package com.zoolandia.app.features.pet.domain;

import com.zoolandia.app.features.client.domain.Client;
import com.zoolandia.app.features.consultation.domain.Consultation;
import com.zoolandia.app.features.pet.validation.ValidPetBreed;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Table(name = "pets")
@Data
@ValidPetBreed
@NoArgsConstructor
@AllArgsConstructor
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private PetType type;

    private String breed;

    private LocalDate birthDate;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client owner;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Consultation> consultations = new ArrayList<>();

    @OneToOne(mappedBy = "pet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private com.zoolandia.app.features.medicalhistory.domain.MedicalHistory medicalHistory;

    @Builder.Default
    private boolean active = true;

    @PostPersist
    private void createMedicalHistory() {
        if (this.medicalHistory == null) {
            this.medicalHistory = com.zoolandia.app.features.medicalhistory.domain.MedicalHistory.builder()
                    .pet(this)
                    .notes("Historial médico creado automáticamente")
                    .build();
        }
    }

}
