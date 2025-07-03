package com.zoolandia.app.features.pet.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.FormService;
import com.vaadin.hilla.crud.ListRepositoryService;
import com.vaadin.hilla.crud.filter.Filter;
import com.zoolandia.app.features.client.domain.Client;
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
import java.util.stream.Collectors;

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
        return petRepository.findAllActive(pageable).stream()
                .map(pet -> new PetSummaryDTO(pet.getId(), pet.getName(), pet.getType(), pet.getBreed(),
                        pet.getBirthDate(),
                        pet.getOwners().isEmpty()
                                ? "Sin dueño"
                                : pet.getOwners().get(0).getFirstName() + " " + pet.getOwners().get(0).getLastName()))
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

    @Transactional
    public Pet mergePets(Long keepPetId, Long removePetId) {
        log.debug("Request to merge pets: keep={}, remove={}", keepPetId, removePetId);

        // Obtener ambas mascotas
        Pet keepPet = petRepository.findById(keepPetId).orElseThrow(() -> new PetNotFoundException(keepPetId));
        Pet removePet = petRepository.findById(removePetId).orElseThrow(() -> new PetNotFoundException(removePetId));

        // Validar que sean la misma mascota (mismo nombre y tipo)
        if (!keepPet.getName().equalsIgnoreCase(removePet.getName())
                || !keepPet.getType().equals(removePet.getType())) {
            throw new IllegalArgumentException("Las mascotas no parecen ser la misma (nombre o tipo diferentes)");
        }

        // Fusionar owners (evitar duplicados por ID)
        Set<Long> existingOwnerIds = keepPet.getOwners().stream().map(Client::getId).collect(Collectors.toSet());

        List<Client> newOwners = removePet.getOwners().stream()
                .filter(owner -> !existingOwnerIds.contains(owner.getId())).toList();

        // Agregar los nuevos owners
        keepPet.getOwners().addAll(newOwners);

        // Desactivar la mascota que se va a "eliminar"
        removePet.setActive(false);

        // Guardar cambios
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

    @Override
    @Transactional(readOnly = true)
    public List<Pet> list(Pageable pageable, @Nullable Filter filter) {
        // Siempre filtrar solo mascotas activas, independientemente del filtro aplicado
        Page<Pet> page = petRepository.findAllActive(pageable);
        List<Pet> content = page.getContent();

        // Asegurar que nunca retornemos null
        return content != null ? content : Collections.emptyList();
    }

}
