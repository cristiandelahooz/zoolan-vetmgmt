package com.zoolandia.app.features.client.repository;

import com.zoolandia.app.features.client.domain.Client;
import com.zoolandia.app.features.client.domain.ClientRating;
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
}
