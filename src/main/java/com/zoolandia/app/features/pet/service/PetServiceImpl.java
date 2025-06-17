package com.zoolandia.app.features.pet.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.FormService;
import com.vaadin.hilla.crud.ListRepositoryService;
import com.zoolandia.app.features.pet.domain.PetType;
import com.zoolandia.app.features.pet.mapper.PetMapper;
import com.zoolandia.app.features.pet.repository.PetRepository;
import com.zoolandia.app.features.pet.domain.Pet;
import com.zoolandia.app.features.pet.service.dto.PetCreateDTO;
import com.zoolandia.app.features.pet.service.dto.PetSummaryDTO;
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
import org.jspecify.annotations.Nullable;

import java.util.*;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@BrowserCallable
@AnonymousAllowed
public class PetServiceImpl extends ListRepositoryService<Pet, Long, PetRepository>
        implements PetService, FormService<PetCreateDTO, Long> {

    private final PetRepository petRepository;
    private final PetMapper petMapper;

    @Override
    @Transactional
    //@PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'VETERINARIAN')")
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

        Pet existingPet = petRepository.findById(id).orElseThrow(() -> new PetNotFoundException(id));

        petMapper.updatePetFromDTO(petDTO, existingPet);

        return petRepository.save(existingPet);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pet> getPetById(Long id) {
        log.debug("Request to get Pet : {}", id);
        return petRepository.findById(id);
    }

    /*
     * @Override
     *
     * @Transactional(readOnly = true) public Page<Pet> getAllPets(Pageable pageable) {
     * log.debug("Request to get all Pets"); return petRepository.findAll(pageable); }
     */

    @Override
    @Transactional(readOnly = true)
    public List<PetSummaryDTO> getAllPets(Pageable pageable) {
        return petRepository.findAll(pageable).stream()
                .map(pet -> new PetSummaryDTO(pet.getId(), pet.getName(), pet.getType(), pet.getBreed(),
                        pet.getBirthDate(), pet.getOwner().getFirstName() + " " + pet.getOwner().getLastName()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Pet> getPetsByOwnerId(Long ownerId, Pageable pageable) {
        log.debug("Request to get Pets by Owner ID: {}", ownerId);
        return petRepository.findByOwnerId(ownerId, pageable);
    }

    @Override
    @Transactional
    // @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        log.debug("Request to delete Pet : {}", id);
        petRepository.deleteById(id);
        log.info("Deleted Pet ID: {}", id);

        log.debug("Request to delete Pet via FormService : {}", id);

        Pet pet = petRepository.findById(id).orElseThrow(() -> new PetNotFoundException(id));

        pet.setActive(false);
        petRepository.save(pet);

        log.info("Pet deactivated via FormService, ID: {}", id);
    }

    @Override
    @Nullable
    public PetCreateDTO save(PetCreateDTO dto) {
        try {
            Pet pet = petMapper.toEntity(dto);
            Pet savedPet = petRepository.save(pet);
            PetCreateDTO result = petMapper.toCreateDTO(savedPet); // necesitas este método
            log.info("Pet created successfully with ID: {}", savedPet.getId());
            return result;
        } catch (Exception e) {
            log.error("Error creating Pet: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Map<String, List<String>> getPetTypeAndBreeds() {
        Map<String, List<String>> map = new HashMap<>();
        for (PetType type : PetType.values()) {
            map.put(type.name(), type.getBreeds());
        }
        return map;
    }

    @Transactional(readOnly = true)
    public List<String> getBreedsByType(PetType petType) {
        return petType.getBreeds();
    }

    @Transactional(readOnly = true)
    public List<String> getAllPetTypes() {
        return Arrays.stream(PetType.values()).map(Enum::name).toList();
    }
}
