package com.wornux.services.interfaces;

import com.wornux.data.entity.Invoice;
import com.wornux.data.entity.Service;
import com.wornux.data.enums.ServiceCategory;
import com.wornux.data.enums.ServiceType;
import com.wornux.data.repository.ServiceRepository;
import com.wornux.dto.request.ServiceCreateRequestDto;
import com.wornux.dto.request.ServiceUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Enhanced service interface for managing veterinary services with consultation integration
 */
public interface ServiceService {

  Service save(Service service);

  Optional<Service> findById(Long id);

  List<Service> findAll();

  Page<Service> findAll(Pageable pageable);

  List<Service> findByCategory(ServiceCategory category);

  List<Service> findActiveServices();

  List<Service> findMedicalServices();

  List<Service> searchByName(String name);

  void delete(Long id);

  long countActiveServices();

  Service save(ServiceCreateRequestDto dto);

  Service updateService(Long id, ServiceUpdateRequestDto dto);

  void deactivateService(Long id);

  List<Service> getAllActiveServices();

  List<Service> getServicesByCategory(ServiceCategory serviceCategory);

  Service getServiceById(Long id);


  ServiceRepository getRepository();
}