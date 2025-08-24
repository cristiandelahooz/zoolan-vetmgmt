package com.wornux.services.interfaces;

import com.wornux.data.entity.Offering;
import com.wornux.data.repository.OfferingRepository;
import com.wornux.dto.request.ServiceCreateRequestDto;
import com.wornux.dto.request.ServiceUpdateRequestDto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Enhanced offering interface for managing veterinary services with consultation integration */
public interface OfferingService {

  Offering save(Offering offering);

  Optional<Offering> findById(Long id);

  List<Offering> findAll();

  Page<Offering> findAll(Pageable pageable);

  List<Offering> findMedicalServices();

  List<Offering> findGroomingServices();

  void delete(Long id);

  Offering save(ServiceCreateRequestDto dto);

  void updateService(Long id, ServiceUpdateRequestDto dto);

  void deactivateService(Long id);

  List<Offering> getAllActiveServices();

  OfferingRepository getRepository();
}
