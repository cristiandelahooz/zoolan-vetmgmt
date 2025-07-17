package com.wornux.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wornux.data.enums.Gender;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.*;
import com.wornux.data.enums.PetType;
import org.hibernate.envers.Audited;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = { "medicalHistory" })
@ToString(exclude = { "medicalHistory" })
@Entity
@Table(name = "pets")
@Audited(withModifiedFlag = true)
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
    @Builder.Default
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
