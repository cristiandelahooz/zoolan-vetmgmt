package com.zoolandia.app.features.medicalHistory.domain;

import com.zoolandia.app.features.consultation.domain.Consultation;
import com.zoolandia.app.features.pet.domain.Pet;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "medical_history")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "pet_id", unique = true)
    private Pet pet;

    @OneToMany(mappedBy = "medicalHistory", cascade = CascadeType.ALL)
    private Set<Consultation> consultations = new HashSet<>();
}
