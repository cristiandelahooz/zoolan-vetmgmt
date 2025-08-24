package com.wornux.data.repository;

import com.wornux.data.entity.InvoiceOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/** Repository for InvoiceOffering entity operations */
@Repository
public interface InvoiceOfferingRepository
    extends JpaRepository<InvoiceOffering, Long>, JpaSpecificationExecutor<InvoiceOffering> {}
