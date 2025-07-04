package com.wornux.features.pet.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wornux.dto.Gender;
import com.wornux.features.client.domain.Client;
import com.wornux.features.medicalHistory.domain.MedicalHistory;
import com.wornux.features.pet.validation.ValidPetBreed;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Builder
@Entity
@Table(name = "pets")
@Data
@ValidPetBreed
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "medicalHistory" })
@ToString(exclude = { "medicalHistory" })
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private PetType type;

    private String breed;

    private LocalDate birthDate;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "pet_owners", joinColumns = @JoinColumn(name = "pet_id"), inverseJoinColumns = @JoinColumn(name = "client_id"))
    private List<Client> owners = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @OneToOne(mappedBy = "pet", cascade = CascadeType.ALL)
    @JsonIgnore
    private MedicalHistory medicalHistory;

    @Builder.Default
    private boolean active = true;

    @PostPersist
    private void createMedicalHistory() {
        this.medicalHistory = MedicalHistory.builder().pet(this).notes("Historial médico creado automáticamente")
                .build();
    }
}
