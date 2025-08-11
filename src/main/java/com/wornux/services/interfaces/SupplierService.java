package com.wornux.services.interfaces;

import com.vaadin.hilla.BrowserCallable;
import com.wornux.data.entity.Supplier;
import com.wornux.data.repository.SupplierRepository;
import com.wornux.dto.request.SupplierCreateRequestDto;
import com.wornux.dto.request.UpdateSupplierRequestDto;
import com.wornux.dto.response.SupplierListDto;
import jakarta.validation.Valid;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.data.domain.Pageable;
import com.vaadin.hilla.crud.filter.Filter;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing Supplier entities.
 */
@BrowserCallable
@Validated
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface SupplierService {

    SupplierCreateRequestDto save(SupplierCreateRequestDto supplierDto);

    Supplier update(UpdateSupplierRequestDto supplierDto);

    void delete(Long id);

    Optional<Supplier> getSupplierById(Long id);

    List<Supplier> getAllSuppliers();

    List<Supplier> list(Pageable pageable, @Nullable Filter filter);

    List<SupplierListDto> listAsDto(Pageable pageable, @Nullable Filter filter);

    SupplierRepository getRepository();

    SupplierCreateRequestDto getCreateDtoById(Long id);

}