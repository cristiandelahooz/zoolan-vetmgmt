package com.wornux.services.implementations;

import com.wornux.data.entity.Offering;
import com.wornux.data.enums.OfferingType;
import com.wornux.data.repository.InvoiceRepository;
import com.wornux.data.repository.InvoiceServiceRepository;
import com.wornux.data.repository.OfferingRepository;
import com.wornux.dto.request.ServiceCreateRequestDto;
import com.wornux.dto.request.ServiceUpdateRequestDto;
import com.wornux.mapper.ServiceMapper;
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
@Component("serviceServiceImpl")
@RequiredArgsConstructor
public class OfferingServiceImpl implements OfferingService {

  private final OfferingRepository serviceRepository;
  private final ServiceMapper serviceMapper;
  private final InvoiceService invoiceService;
  private final InvoiceServiceRepository invoiceServiceRepository;
  private final InvoiceRepository invoiceRepository;

  @Override
  @Transactional
  public Offering save(Offering offering) {
    log.debug("Saving offering: {}", offering.getName());
    return serviceRepository.save(offering);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Offering> findById(Long id) {
    return serviceRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Offering> findAll() {
    return serviceRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Offering> findAll(Pageable pageable) {
    return serviceRepository.findAll(pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Offering> findByServiceType(OfferingType serviceType) {
    return serviceRepository.findByServiceTypeAndActiveTrue(serviceType);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Offering> findActiveServices() {
    return serviceRepository.findByActiveTrue();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Offering> searchByName(String name) {
    return serviceRepository.findByNameContainingIgnoreCaseAndActiveTrue(name);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    log.debug("Deleting offering with id: {}", id);
    serviceRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public long countActiveServices() {
    return serviceRepository.countByActiveTrue();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Offering> findMedicalServices() {
    return serviceRepository.findByServiceTypeAndActiveTrue(OfferingType.MEDICAL);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Offering> findGroomingServices() {
    return serviceRepository.findByServiceTypeAndActiveTrue(OfferingType.GROOMING);
  }

  @Override
  @Transactional
  public Offering save(@Valid ServiceCreateRequestDto dto) {
    try {
      log.debug("Request to save Offering: {}", dto);

      // Check for duplicate offering name
      if (serviceRepository.existsByNameAndActiveTrue(dto.getName())) {
        throw new ValidationException("Ya existe un servicio activo con ese nombre");
      }

      Offering offering = serviceMapper.toEntity(dto);
      offering.setActive(true);
      offering = serviceRepository.save(offering);

      log.info("Offering saved successfully with ID: {}", offering.getId());
      return offering;
    } catch (Exception e) {
      log.error("Error saving Offering: {}", e.getMessage());
      throw e;
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Offering updateService(@NonNull Long id, @Valid ServiceUpdateRequestDto dto) {
    log.debug("Request to update Offering with ID: {}", id);

    Offering offering =
        serviceRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado con ID: " + id));

    // Validate unique name (excluding current offering)
    if (dto.getName() != null && !dto.getName().equals(offering.getName())) {
      serviceRepository
          .findByNameAndActiveTrueAndIdNot(dto.getName(), id)
          .ifPresent(
              existing -> {
                throw new ValidationException("Ya existe un servicio activo con ese nombre");
              });
    }

    serviceMapper.updateServiceFromDto(dto, offering);
    offering = serviceRepository.save(offering);

    log.info("Offering updated successfully with ID: {}", id);
    return offering;
  }

  @Override
  @Transactional
  public void deactivateService(@NonNull Long id) {
    log.debug("Request to deactivate Offering with ID: {}", id);

    Offering offering =
        serviceRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado con ID: " + id));

    offering.setActive(false);
    serviceRepository.save(offering);

    log.info("Offering deactivated successfully with ID: {}", id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Offering> getAllActiveServices() {
    log.debug("Request to get all active services");
    return serviceRepository.findByActiveTrueOrderByNameAsc();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Offering> getServicesByType(OfferingType serviceType) {
    log.debug("Request to get services by type: {}", serviceType);
    return serviceRepository.findByServiceTypeAndActiveTrue(serviceType);
  }

  @Override
  @Transactional(readOnly = true)
  public Offering getServiceById(@NonNull Long id) {
    log.debug("Request to get Offering with ID: {}", id);
    return serviceRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado con ID: " + id));
  }

  @Override
  public OfferingRepository getRepository() {
    return this.serviceRepository;
  }
}
