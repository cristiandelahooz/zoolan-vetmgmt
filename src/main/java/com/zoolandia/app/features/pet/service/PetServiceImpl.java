package com.zoolandia.app.features.pet.service;

import com.zoolandia.app.features.pet.mapper.PetMapper;
import com.zoolandia.app.features.pet.repository.PetRepository;
import com.zoolandia.app.features.pet.domain.Pet;
import com.zoolandia.app.features.pet.service.dto.PetCreateDTO;
import com.zoolandia.app.features.pet.service.dto.PetUpdateDTO;
import com.zoolandia.app.features.pet.service.exception.PetNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {

    private final PetRepository petRepository;
    private final PetMapper petMapper;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'VETERINARIAN')")
    public Pet createPet(@Valid PetCreateDTO petDTO) {
        log.debug("Request to create Pet : {}", petDTO);

        Pet pet = petMapper.toEntity(petDTO);
        pet = petRepository.save(pet);

        log.info("Created Pet with ID: {}", pet.getId());
        return pet;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'VETERINARIAN')")
    public Pet updatePet(Long id, @Valid PetUpdateDTO petDTO) {
        log.debug("Request to update Pet : {}", petDTO);

        Pet existingPet = petRepository.findById(id)
                .orElseThrow(() -> new PetNotFoundException(id));

        petMapper.updatePetFromDTO(petDTO, existingPet);

        return petRepository.save(existingPet);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pet> getPetById(Long id) {
        log.debug("Request to get Pet : {}", id);
        return petRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Pet> getAllPets(Pageable pageable) {
        log.debug("Request to get all Pets");
        return petRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Pet> getPetsByOwnerId(Long ownerId, Pageable pageable) {
        log.debug("Request to get Pets by Owner ID: {}", ownerId);
        return petRepository.findByOwnerId(ownerId, pageable);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deletePet(Long id) {
        log.debug("Request to delete Pet : {}", id);
        petRepository.deleteById(id);
        log.info("Deleted Pet ID: {}", id);
    }
}

