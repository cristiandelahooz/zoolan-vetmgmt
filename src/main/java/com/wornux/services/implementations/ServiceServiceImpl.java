package com.wornux.services.implementations;

import com.wornux.data.entity.Service;
import com.wornux.data.enums.ServiceCategory;
import com.wornux.data.repository.InvoiceRepository;
import com.wornux.data.repository.InvoiceServiceRepository;
import com.wornux.data.repository.ServiceRepository;
import com.wornux.dto.request.ServiceCreateRequestDto;
import com.wornux.dto.request.ServiceUpdateRequestDto;
import com.wornux.mapper.ServiceMapper;
import com.wornux.services.interfaces.ServiceService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Implementation of ServiceService for managing veterinary services */
@Slf4j
@Component("serviceServiceImpl")
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {

  private final ServiceRepository serviceRepository;
  private final ServiceMapper serviceMapper;
  private final InvoiceService invoiceService;
  private final InvoiceServiceRepository invoiceServiceRepository;
  private final InvoiceRepository invoiceRepository;

  @Override
  @Transactional
  public Service save(Service service) {
    log.debug("Saving service: {}", service.getName());
    return serviceRepository.save(service);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Service> findById(Long id) {
    return serviceRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Service> findAll() {
    return serviceRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Service> findAll(Pageable pageable) {
    return serviceRepository.findAll(pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Service> findByCategory(ServiceCategory category) {
    return serviceRepository.findByServiceCategoryAndActiveTrue(category);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Service> findActiveServices() {
    return serviceRepository.findByActiveTrue();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Service> searchByName(String name) {
    return serviceRepository.findByNameContainingIgnoreCaseAndActiveTrue(name);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    log.debug("Deleting service with id: {}", id);
    serviceRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public long countActiveServices() {
    return serviceRepository.countByActiveTrue();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Service> findMedicalServices() {
    return serviceRepository.findByServiceCategoryAndActiveTrue(ServiceCategory.MEDICAL);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Service> findGroomingServices() {
    return serviceRepository.findByServiceCategoryAndActiveTrue(ServiceCategory.GROOMING);
  }

  @Override
  @Transactional
  public Service save(@Valid ServiceCreateRequestDto dto) {
    try {
      log.debug("Request to save Service: {}", dto);

      // Check for duplicate service name
      if (serviceRepository.existsByNameAndActiveTrue(dto.getName())) {
        throw new ValidationException("Ya existe un servicio activo con ese nombre");
      }

      Service service = serviceMapper.toEntity(dto);
      service.setActive(true);
      service = serviceRepository.save(service);

      log.info("Service saved successfully with ID: {}", service.getId());
      return service;
    } catch (Exception e) {
      log.error("Error saving Service: {}", e.getMessage());
      throw e;
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Service updateService(@NonNull Long id, @Valid ServiceUpdateRequestDto dto) {
    log.debug("Request to update Service with ID: {}", id);

    Service service =
        serviceRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado con ID: " + id));

    // Validate unique name (excluding current service)
    if (dto.getName() != null && !dto.getName().equals(service.getName())) {
      serviceRepository
          .findByNameAndActiveTrueAndIdNot(dto.getName(), id)
          .ifPresent(
              existing -> {
                throw new ValidationException("Ya existe un servicio activo con ese nombre");
              });
    }

    serviceMapper.updateServiceFromDto(dto, service);
    service = serviceRepository.save(service);

    log.info("Service updated successfully with ID: {}", id);
    return service;
  }

  @Override
  @Transactional
  public void deactivateService(@NonNull Long id) {
    log.debug("Request to deactivate Service with ID: {}", id);

    Service service =
        serviceRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado con ID: " + id));

    service.setActive(false);
    serviceRepository.save(service);

    log.info("Service deactivated successfully with ID: {}", id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Service> getAllActiveServices() {
    log.debug("Request to get all active services");
    return serviceRepository.findByActiveTrueOrderByNameAsc();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Service> getServicesByCategory(ServiceCategory serviceCategory) {
    log.debug("Request to get services by type: {}", serviceCategory);
    return serviceRepository.findByServiceCategoryAndActiveTrue(serviceCategory);
  }

  @Override
  @Transactional(readOnly = true)
  public Service getServiceById(@NonNull Long id) {
    log.debug("Request to get Service with ID: {}", id);
    return serviceRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado con ID: " + id));
  }

  @Override
  public ServiceRepository getRepository() {
    return this.serviceRepository;
  }
}
