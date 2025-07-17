package com.wornux.services.interfaces;

import com.vaadin.hilla.BrowserCallable;
import com.wornux.data.entity.Consultation;
import com.wornux.dto.request.CreateConsultationRequestDto;
import com.wornux.dto.request.UpdateConsultationRequestDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@BrowserCallable
@Validated
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ConsultationService {

    /**
     * Create a new consultation
     *
     * @param createDTO
     *            the consultation data
     * @return the created consultation
     */
    Consultation create(CreateConsultationRequestDto createDTO);

    /**
     * Update an existing consultation
     *
     * @param id
     *            the id of the consultation to update
     * @param updateDTO
     *            the updated consultation data
     * @return the updated consultation
     */
    Consultation update(Long id, UpdateConsultationRequestDto updateDTO);

    /**
     * Partially update a consultation
     *
     * @param id
     *            the id of the consultation to update
     * @param updateDTO
     *            the consultation fields to update
     * @return the updated consultation
     */
    Consultation partialUpdate(Long id, UpdateConsultationRequestDto updateDTO);

    /**
     * Get a consultation by id
     *
     * @param id
     *            the consultation id
     * @return the found consultation
     */
    Consultation findById(Long id);

    /**
     * Get all consultations with pagination
     *
     * @param pageable
     *            pagination information
     * @return page of consultations
     */
    Page<Consultation> findAll(Pageable pageable);

    /**
     * Get all consultations for a specific pet
     *
     * @param petId
     *            the pet id
     * @return list of consultations
     */
    List<Consultation> findByPetId(Long petId);

    /**
     * Get all consultations for a specific veterinarian
     *
     * @param veterinarianId
     *            the veterinarian id
     * @return list of consultations
     */
    List<Consultation> findByVeterinarianId(Long veterinarianId);

    /**
     * Delete a consultation
     *
     * @param id
     *            the consultation id to delete
     */
    void delete(Long id);

    Page<Consultation> findByActiveTrue(Pageable pageable);

}