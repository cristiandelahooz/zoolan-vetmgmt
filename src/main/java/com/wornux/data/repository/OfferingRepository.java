package com.wornux.data.repository;

import com.wornux.data.entity.Offering;
import com.wornux.data.enums.OfferingType;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Repository for Offering entity operations */
@Repository
public interface OfferingRepository
    extends JpaRepository<Offering, Long>, JpaSpecificationExecutor<Offering> {

  /** Find all active services */
  List<Offering> findByActiveTrue();

  /** Find services by category */
  List<Offering> findByServiceTypeAndActiveTrue(OfferingType serviceType);

  /** Find services by name containing (case insensitive) */
  @Query(
      "SELECT s FROM Offering s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) AND s.active = true")
  List<Offering> findByNameContainingIgnoreCaseAndActiveTrue(@Param("name") String name);

  /** Count active services */
  long countByActiveTrue();

  @Query("SELECT s FROM Offering s WHERE s.active = true ORDER BY s.name ASC")
  List<Offering> findAllActiveServices();

  @Query(
      "SELECT s FROM Offering s WHERE s.active = true AND s.serviceType = :serviceType ORDER BY s.name ASC")
  List<Offering> findActiveServicesByServiceType(@Param("serviceType") OfferingType serviceType);

  @Query(
      "SELECT s FROM Offering s WHERE s.active = true AND (s.name LIKE %:searchTerm% OR s.description LIKE %:searchTerm%) ORDER BY s.name ASC")
  List<Offering> findActiveServicesByNameOrDescription(@Param("searchTerm") String searchTerm);

  boolean existsByNameAndActiveTrue(String name);

  Optional<Object> findByNameAndActiveTrueAndIdNot(String name, @NonNull Long id);

  List<Offering> findByActiveTrueOrderByNameAsc();

  List<Offering> findByServiceTypeAndActiveTrueOrderByNameAsc(OfferingType serviceType);
}
