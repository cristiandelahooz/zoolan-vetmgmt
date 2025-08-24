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
  Optional<Invoice> findByConsultation(Consultation consultation);

  Optional<Invoice> findByGrooming_Id(Long groomingId);

  @Query("""
      select distinct i
      from Invoice i
      left join fetch i.services s
      left join fetch s.service
      left join fetch i.products p
      left join fetch p.product
      where i.consultation.id = :consultationId
      """)
  Optional<Invoice> findByConsultationIdWithDetails(@Param("consultationId") Long consultationId);

  @Query("""
           select i from Invoice i
           left join fetch i.services
           left join fetch i.products
           where i.grooming.id = :groomingId
           """)
  Optional<Invoice> findByGroomingIdWithDetails(@Param("groomingId") Long groomingId);
}
