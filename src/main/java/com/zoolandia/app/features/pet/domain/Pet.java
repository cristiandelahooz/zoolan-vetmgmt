package com.zoolandia.app.features.pet.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zoolandia.app.features.client.domain.Client;
import com.zoolandia.app.features.medicalHistory.domain.MedicalHistory;
import com.zoolandia.app.features.pet.validation.ValidPetBreed;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;


@Builder
@Entity
@Table(name = "pets")
@Data
@ValidPetBreed
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"medicalHistory"})
@ToString(exclude = {"medicalHistory"})
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

    @OneToOne(mappedBy = "pet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private MedicalHistory medicalHistory;

    @Builder.Default
    private boolean active = true;

    @PostPersist
    private void createMedicalHistory() {
        if (this.medicalHistory == null) {
            this.medicalHistory = MedicalHistory.builder()
                    .pet(this)
                    .notes("Historial médico creado automáticamente")
                    .build();
        }
    }
}
