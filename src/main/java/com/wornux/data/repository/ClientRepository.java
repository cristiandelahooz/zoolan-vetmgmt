package com.wornux.data.repository;

import com.wornux.data.entity.Client;
import com.wornux.data.enums.ClientRating;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository
    extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {
  Optional<Client> findByCedula(String cedula);

  Optional<Client> findByCedulaAndIdNot(String cedula, Long id);

  Optional<Client> findByPassport(String passport);

  Optional<Client> findByPassportAndIdNot(String passport, Long id);

  Optional<Client> findByRnc(String rnc);

  Optional<Client> findByRncAndIdNot(String rnc, Long id);

  boolean existsByCedula(String cedula);

  boolean existsByPassport(String passport);

  boolean existsByRnc(String rnc);

  Page<Client> findByRating(ClientRating rating, Pageable pageable);

  Page<Client> findByProvince(String province, Pageable pageable);

  List<Client> findAllByActiveTrue();

  List<Client> findByIdInAndActiveTrue(Collection<Long> ids);

  Optional<Client> findByEmail(String email);

  @Query(
      "SELECT COUNT(c) FROM Client c WHERE c.createdAt >= CAST(:startDate AS timestamp) AND c.createdAt < CAST(:endDate AS timestamp)")
  Long countNewClientsByPeriod(
      @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  @Query(
      "SELECT COUNT(DISTINCT i.client) FROM Invoice i WHERE i.createdDate >= CAST(:startDate AS timestamp) AND i.createdDate < CAST(:endDate AS timestamp) "
          + "AND EXISTS (SELECT 1 FROM Invoice i2 WHERE i2.client = i.client AND i2.createdDate < CAST(:startDate AS timestamp))")
  Long countReturningClientsByPeriod(
      @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  @Query("SELECT COUNT(c) FROM Client c WHERE c.active = true")
  Long countActiveClients();
}
