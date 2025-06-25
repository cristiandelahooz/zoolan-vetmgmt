package com.zoolandia.app.features.pet.domain;

import com.zoolandia.app.features.client.domain.Client;
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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "pet_owners", joinColumns = @JoinColumn(name = "pet_id"), inverseJoinColumns = @JoinColumn(name = "client_id"))
    private List<Client> owners = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Builder.Default
    private boolean active = true;
}
