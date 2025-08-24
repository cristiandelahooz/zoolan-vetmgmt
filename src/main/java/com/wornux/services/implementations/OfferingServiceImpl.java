package com.wornux.services.implementations;

import com.wornux.data.entity.Offering;
import com.wornux.data.enums.OfferingType;
import com.wornux.data.repository.OfferingRepository;
import com.wornux.dto.request.ServiceCreateRequestDto;
import com.wornux.dto.request.ServiceUpdateRequestDto;
import com.wornux.mapper.OfferingMapper;
import com.wornux.services.interfaces.OfferingService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Implementation of OfferingService for managing veterinary services */
@Slf4j
@Component("offeringServiceImpl")
@RequiredArgsConstructor
public class OfferingServiceImpl implements OfferingService {

  private final OfferingRepository offeringRepository;
  private final OfferingMapper offeringMapper;

  @Override
  @Transactional
  public Offering save(Offering offering) {
    log.debug("Saving offering: {}", offering.getName());
    return offeringRepository.save(offering);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Offering> findById(Long id) {
    return offeringRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Offering> findAll() {
    return offeringRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Offering> findAll(Pageable pageable) {
    return offeringRepository.findAll(pageable);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    log.debug("Deleting offering with id: {}", id);
    offeringRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Offering> findMedicalServices() {
    return offeringRepository.findByOfferingTypeAndActiveTrue(OfferingType.MEDICAL);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Offering> findGroomingServices() {
    return offeringRepository.findByOfferingTypeAndActiveTrue(OfferingType.GROOMING);
  }

  @Override
  @Transactional
  public Offering save(@Valid ServiceCreateRequestDto dto) {
    try {
      log.debug("Request to save Offering: {}", dto);

      // Check for duplicate offering name
      if (offeringRepository.existsByNameAndActiveTrue(dto.getName())) {
        throw new ValidationException("Ya existe un servicio activo con ese nombre");
      }

      Offering offering = offeringMapper.toEntity(dto);
      offering.setActive(true);
      offering = offeringRepository.save(offering);

      log.info("Offering saved successfully with ID: {}", offering.getId());
      return offering;
    } catch (Exception e) {
      log.error("Error saving Offering: {}", e.getMessage());
      throw e;
    }
  }

  @Override
  @Transactional(readOnly = true)
  public void updateService(@NonNull Long id, @Valid ServiceUpdateRequestDto dto) {
    log.debug("Request to update Offering with ID: {}", id);

    Offering offering =
        offeringRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado con ID: " + id));

    // Validate unique name (excluding current offering)
    if (dto.getName() != null && !dto.getName().equals(offering.getName())) {
      offeringRepository
          .findByNameAndActiveTrueAndIdNot(dto.getName(), id)
          .ifPresent(
              existing -> {
                throw new ValidationException("Ya existe un servicio activo con ese nombre");
              });
    }

    offeringMapper.updateServiceFromDto(dto, offering);
    offering = offeringRepository.save(offering);

    log.info("Offering updated successfully with ID: {}", id);
  }

  @Override
  @Transactional
  public void deactivateService(@NonNull Long id) {
    log.debug("Request to deactivate Offering with ID: {}", id);

    Offering offering =
        offeringRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado con ID: " + id));

    offering.setActive(false);
    offeringRepository.save(offering);

    log.info("Offering deactivated successfully with ID: {}", id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Offering> getAllActiveServices() {
    log.debug("Request to get all active services");
    return offeringRepository.findByActiveTrueOrderByNameAsc();
  }

  @Override
  public OfferingRepository getRepository() {
    return this.offeringRepository;
  }
}
