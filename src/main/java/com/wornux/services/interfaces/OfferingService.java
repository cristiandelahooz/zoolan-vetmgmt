package com.wornux.services.interfaces;

import com.wornux.data.entity.Offering;
import com.wornux.data.enums.OfferingType;
import com.wornux.data.repository.OfferingRepository;
import com.wornux.dto.request.ServiceCreateRequestDto;
import com.wornux.dto.request.ServiceUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/** Enhanced offering interface for managing veterinary services with consultation integration */
public interface OfferingService {

  Offering save(Offering offering);

  Optional<Offering> findById(Long id);

  List<Offering> findAll();

  Page<Offering> findAll(Pageable pageable);

  List<Offering> findByServiceType(OfferingType serviceType);

  List<Offering> findActiveServices();

  List<Offering> findMedicalServices();

  List<Offering> findGroomingServices();

  List<Offering> searchByName(String name);

  void delete(Long id);

  long countActiveServices();

  Offering save(ServiceCreateRequestDto dto);

  Offering updateService(Long id, ServiceUpdateRequestDto dto);

  void deactivateService(Long id);

  List<Offering> getAllActiveServices();

  List<Offering> getServicesByType(OfferingType serviceType);

  Offering getServiceById(Long id);

  OfferingRepository getRepository();
}
