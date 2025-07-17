package com.wornux.services.implementations;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.FormService;
import com.vaadin.hilla.crud.ListRepositoryService;
import com.vaadin.hilla.crud.filter.Filter;
import com.wornux.data.entity.Client;
import com.wornux.data.entity.Consultation;
import com.wornux.data.repository.ConsultationRepository;
import com.wornux.data.entity.Pet;

import com.wornux.data.enums.PetType;
import com.wornux.dto.request.PetCreateRequestDto;
import com.wornux.mapper.PetMapper;
import com.wornux.data.repository.PetRepository;
import com.wornux.dto.response.PetSummaryResponseDto;
import com.wornux.dto.request.PetUpdateRequestDto;
import com.wornux.exception.PetNotFoundException;
import com.wornux.services.interfaces.PetService;
import com.wornux.services.interfaces.ClientService;
import jakarta.validation.Valid;

import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@BrowserCallable
@Transactional
@AnonymousAllowed
public class PetServiceImpl extends ListRepositoryService<Pet, Long, PetRepository>
        implements PetService, FormService<PetCreateRequestDto, Long> {

    private final PetRepository petRepository;
    private final PetMapper petMapper;
    private final ConsultationRepository consultationRepository;
    private final ClientService clientService;

    @Override
    @Transactional(readOnly = true)
    public List<Pet> list(Pageable pageable, @Nullable Filter filter) {
        log.debug("Request to list active Pets with pageable: {} and filter: {}", pageable, filter);

        Page<Pet> page = petRepository.findByActiveTrueOrderByNameAsc(pageable);
        return page.getContent();
    }

    @Override
    @Transactional
    //@PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'VETERINARIAN')")
    public Pet createPet(@Valid PetCreateRequestDto petDTO) {
        log.debug("Request to create Pet : {}", petDTO);

        Pet pet = petMapper.toEntity(petDTO);
        pet = petRepository.save(pet);

        log.info("Created Pet with ID: {}", pet.getId());
        return pet;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'VETERINARIAN')")
    public Pet updatePet(Long id, @Valid PetUpdateRequestDto petDTO) {
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
    public List<PetSummaryResponseDto> getAllPets(Pageable pageable) {
        return petRepository.findByActiveTrueOrderByNameAsc(pageable).stream()
                .map(pet -> new PetSummaryResponseDto(pet.getId(), pet.getName(), pet.getType(), pet.getBreed(),
                        pet.getBirthDate(), pet.getOwners().isEmpty() ? "Sin dueÃ±o" : pet.getOwners().get(0)
                                                                                               .getFirstName() + " " + pet.getOwners()
                                                                                               .get(0).getLastName()))
                .toList();
    }

    @Override
    public Page<Pet> getPetsByOwnerId(Long ownerId, Pageable pageable) {
        log.debug("Request to get Pets by Owner ID: {}", ownerId);
        return petRepository.findByOwnerId(ownerId, pageable);
    }

    @Override
    @Transactional
    // @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        log.debug("Request to delete Pet : {}", id);

        Pet pet = petRepository.findById(id).orElseThrow(() -> new PetNotFoundException(id));
        pet.setActive(false);
        petRepository.save(pet);

        log.info("Pet deactivated, ID: {}", id);
    }

    @Override
    @Nullable
    public PetCreateRequestDto save(PetCreateRequestDto dto) {
        try {
            Pet pet = petMapper.toEntity(dto);
            Pet savedPet = petRepository.save(pet);
            PetCreateRequestDto result = petMapper.toCreateDTO(savedPet);
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
    public List<Consultation> getConsultationsByPetId(Long petId) {
        log.debug("Request to get consultations for Pet: {}", petId);
        return consultationRepository.findByPetIdAndActiveTrue(petId);
    }

    @Transactional
    public Pet mergePets(Long keepPetId, Long removePetId) {
        log.debug("Request to merge pets: keep={}, remove={}", keepPetId, removePetId);

        Pet keepPet = petRepository.findById(keepPetId).orElseThrow(() -> new PetNotFoundException(keepPetId));
        Pet removePet = petRepository.findById(removePetId).orElseThrow(() -> new PetNotFoundException(removePetId));

        if (!keepPet.getName().equalsIgnoreCase(removePet.getName()) || !keepPet.getType()
                .equals(removePet.getType())) {
            throw new IllegalArgumentException("Las mascotas no parecen ser la misma (nombre o tipo diferentes)");
        }

        Set<Long> existingOwnerIds = keepPet.getOwners().stream().map(Client::getId).collect(Collectors.toSet());

        List<Client> newOwners = removePet.getOwners().stream()
                .filter(owner -> !existingOwnerIds.contains(owner.getId())).toList();

        keepPet.getOwners().addAll(newOwners);
        removePet.setActive(false);

        petRepository.save(keepPet);
        petRepository.save(removePet);

        log.info("Merged pets: {} total owners now associated with pet ID {}", keepPet.getOwners().size(), keepPetId);

        return keepPet;
    }

    @Transactional(readOnly = true)
    public List<Pet> findSimilarPetsByName(String name) {
        log.debug("Searching for pets with name containing: {}", name);
        return petRepository.findSimilarPetsByName(name);
    }
}