package com.wornux.data.repository;

import com.wornux.data.entity.Invoice;
import com.wornux.data.entity.ServiceInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for InvoiceService entity operations
 */
@Repository
public interface InvoiceServiceRepository extends JpaRepository<ServiceInvoice, Long>, JpaSpecificationExecutor<ServiceInvoice> {

  List<ServiceInvoice> findByInvoiceCode(Long invoiceCode);

  void deleteByInvoiceCode(Long invoiceCode);

  @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.services WHERE i.code = :id")
  Optional<Invoice> findByIdWithServices(@Param("id") Long id);
}