package com.wornux.features.pet.service;

import com.vaadin.hilla.BrowserCallable;
import com.wornux.features.consultation.domain.Consultation;
import com.wornux.features.pet.domain.Pet;
import com.wornux.features.pet.service.dto.PetCreateDTO;
import com.wornux.features.pet.service.dto.PetSummaryDTO;
import com.wornux.features.pet.service.dto.PetUpdateDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link Pet} entities.
 */
@BrowserCallable
@Validated
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface PetService {

    /**
     * Creates a new Pet.
     *
     * @param petDTO the DTO containing pet data
     * @return the created Pet entity
     */
    Pet createPet(@Valid PetCreateDTO petDTO);

    /**
     * Updates an existing Pet.
     *
     * @param id     the ID of the pet to update
     * @param petDTO the DTO containing updated data
     * @return the updated Pet entity
     */
    Pet updatePet(Long id, @Valid PetUpdateDTO petDTO);

    /**
     * Retrieves a Pet by ID.
     *
     * @param id the ID of the pet
     * @return the Pet entity if found
     */
    Optional<Pet> getPetById(Long id);

    /**
     * Retrieves all Pets with pagination.
     *
     * @param pageable pagination information
     * @return paginated list of Pets
     */
    List<PetSummaryDTO> getAllPets(Pageable pageable);

    /**
     * Retrieves Pets by owner ID.
     *
     * @param ownerId  the ID of the client who owns the pets
     * @param pageable pagination information
     * @return paginated list of Pets belonging to the given owner
     */
    Page<Pet> getPetsByOwnerId(Long ownerId, Pageable pageable);

    /**
     * Permanently deletes a Pet.
     *
     * @param id the ID of the pet to delete
     */
    void delete(Long id);

    List<Consultation> getConsultationsByPetId(Long petId);
}
