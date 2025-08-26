package com.wornux.services.interfaces;

import com.vaadin.hilla.BrowserCallable;
import com.wornux.data.entity.Client;
import com.wornux.data.entity.Consultation;
import com.wornux.data.entity.Pet;
import com.wornux.data.repository.PetRepository;
import com.wornux.dto.request.PetCreateRequestDto;
import com.wornux.dto.request.PetUpdateRequestDto;
import com.wornux.dto.response.PetSummaryResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/** Offering Interface for managing {@link Pet} entities. */
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
  Pet createPet(@Valid PetCreateRequestDto petDTO);

  /**
   * Updates an existing Pet.
   *
   * @param id the ID of the pet to update
   * @param petDTO the DTO containing updated data
   * @return the updated Pet entity
   */
  Pet updatePet(Long id, @Valid PetUpdateRequestDto petDTO);

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
  List<PetSummaryResponseDto> getAllPets(Pageable pageable);

  /** */
  List<Pet> getAllPets();

  /**
   * Retrieves Pets by owner ID.
   *
   * @param ownerId the ID of the client who owns the pets
   * @param pageable pagination information
   * @return paginated list of Pets belonging to the given owner
   */
  Page<Pet> getPetsByOwnerId(Long ownerId, Pageable pageable);

  List<Pet> getPetsByOwnerId2(Long ownerId);

  List<Pet> getPetsByOwner(Long ownerId);

  /**
   * Permanently deletes a Pet.
   *
   * @param id the ID of the pet to delete
   */
  void delete(Long id);

  List<Consultation> getConsultationsByPetId(Long petId);

  PetRepository getRepository();

  List<Pet> finAllByOwenersContaining(Client client);
}
