package com.wornux.services.interfaces;

import com.wornux.data.entity.Service;
import com.wornux.data.enums.ServiceType;
import com.wornux.data.repository.ServiceRepository;
import com.wornux.dto.request.ServiceCreateRequestDto;
import com.wornux.dto.request.ServiceUpdateRequestDto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Enhanced service interface for managing veterinary services with consultation integration */
public interface ServiceService {

  Service save(Service service);

  Optional<Service> findById(Long id);

  List<Service> findAll();

  Page<Service> findAll(Pageable pageable);

  List<Service> findByServiceType(ServiceType serviceType);

  List<Service> findActiveServices();

  List<Service> findMedicalServices();

  List<Service> findGroomingServices();

  List<Service> searchByName(String name);

  void delete(Long id);

  long countActiveServices();

  Service save(ServiceCreateRequestDto dto);

  Service updateService(Long id, ServiceUpdateRequestDto dto);

  void deactivateService(Long id);

  List<Service> getAllActiveServices();

  List<Service> getServicesByType(ServiceType serviceType);

  Service getServiceById(Long id);

  ServiceRepository getRepository();
}
