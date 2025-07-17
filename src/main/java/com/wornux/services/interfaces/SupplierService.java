package com.wornux.services.interfaces;

import com.vaadin.hilla.BrowserCallable;
import com.wornux.data.entity.Supplier;
import com.wornux.dto.request.SupplierCreateRequestDto;
import com.wornux.dto.request.UpdateSupplierRequestDto;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing Supplier entities.
 */
@BrowserCallable
@Validated
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface SupplierService {

    SupplierCreateRequestDto save(@Valid SupplierCreateRequestDto supplierDto);

    Supplier update(@Valid UpdateSupplierRequestDto supplierDto);

    void delete(Long id);

    Optional<Supplier> getSupplierById(Long id);

    List<Supplier> getAllSuppliers();
}
