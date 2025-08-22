package com.wornux.data.repository;

import com.wornux.data.entity.ServiceInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/** Repository for InvoiceService entity operations */
@Repository
public interface InvoiceServiceRepository
    extends JpaRepository<ServiceInvoice, Long>, JpaSpecificationExecutor<ServiceInvoice> {}
