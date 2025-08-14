package com.wornux.data.repository;

import com.wornux.data.entity.Client;
import com.wornux.data.enums.ClientRating;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository
    extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {
  Optional<Client> findByCedula(String cedula);

  Optional<Client> findByPassport(String passport);

  Optional<Client> findByRnc(String rnc);

  boolean existsByCedula(String cedula);

  boolean existsByPassport(String passport);

  boolean existsByRnc(String rnc);

  Page<Client> findByRating(ClientRating rating, Pageable pageable);

  Page<Client> findByProvince(String province, Pageable pageable);

  List<Client> findAllByActiveTrue();
}
