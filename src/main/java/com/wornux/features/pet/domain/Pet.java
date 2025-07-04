package com.wornux.features.pet.domain;

import com.wornux.dto.Gender;
import com.wornux.features.client.domain.Client;
import com.wornux.features.pet.validation.ValidPetBreed;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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

    @Builder.Default
    private boolean active = true;
}
