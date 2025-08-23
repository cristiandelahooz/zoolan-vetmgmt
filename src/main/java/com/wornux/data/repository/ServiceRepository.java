package com.wornux.data.repository;

import com.wornux.data.entity.Service;
import com.wornux.data.enums.ServiceType;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for Service entity operations */
@Repository
public interface ServiceRepository
    extends JpaRepository<Service, Long>, JpaSpecificationExecutor<Service> {

  /** Find all active services */
  List<Service> findByActiveTrue();

  /** Find services by category */
  List<Service> findByServiceTypeAndActiveTrue(ServiceType serviceType);

  /** Find services by name containing (case insensitive) */
  @Query(
      "SELECT s FROM Service s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) AND s.active = true")
  List<Service> findByNameContainingIgnoreCaseAndActiveTrue(@Param("name") String name);

  /** Count active services */
  long countByActiveTrue();

  @Query("SELECT s FROM Service s WHERE s.active = true ORDER BY s.name ASC")
  List<Service> findAllActiveServices();

  @Query(
      "SELECT s FROM Service s WHERE s.active = true AND s.serviceType = :serviceType ORDER BY s.name ASC")
  List<Service> findActiveServicesByServiceType(@Param("serviceType") ServiceType serviceType);

  @Query(
      "SELECT s FROM Service s WHERE s.active = true AND (s.name LIKE %:searchTerm% OR s.description LIKE %:searchTerm%) ORDER BY s.name ASC")
  List<Service> findActiveServicesByNameOrDescription(@Param("searchTerm") String searchTerm);

  boolean existsByNameAndActiveTrue(String name);

  Optional<Object> findByNameAndActiveTrueAndIdNot(String name, @NonNull Long id);

  List<Service> findByActiveTrueOrderByNameAsc();

  List<Service> findByServiceTypeAndActiveTrueOrderByNameAsc(ServiceType serviceType);
}
