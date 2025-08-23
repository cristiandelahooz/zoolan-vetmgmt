package com.wornux.data.repository;

import com.wornux.data.entity.Consultation;
import com.wornux.data.entity.Invoice;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceRepository
    extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {

  @EntityGraph(attributePaths = {"client", "products.product", "services.service"})
  @Query("SELECT i FROM Invoice i WHERE i.code = :code")
  Optional<Invoice> findByCodeWithServicesAndProducts(@Param("code") Long code);

  @EntityGraph(attributePaths = {"client", "products.product", "services.service"})
  @Query("SELECT i FROM Invoice i WHERE i.consultation = :consultation AND i.active = true")
  Optional<Invoice> findByConsultation(@Param("consultation") Consultation consultation);
}
