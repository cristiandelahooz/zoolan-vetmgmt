package com.wornux.service.implementations;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.FormService;
import com.vaadin.hilla.crud.ListRepositoryService;
import com.vaadin.hilla.crud.filter.Filter;
import com.wornux.data.entity.Supplier;
import com.wornux.data.repository.SupplierRepository;
import com.wornux.dto.request.SupplierCreateRequestDto;
import com.wornux.dto.request.UpdateSupplierRequestDto;
import com.wornux.mapper.SupplierMapper;
import com.wornux.service.interfaces.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@BrowserCallable
@AnonymousAllowed
@Transactional
public class SupplierServiceImpl extends ListRepositoryService<Supplier, Long, SupplierRepository>
        implements SupplierService, FormService<SupplierCreateRequestDto, Long> {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Supplier> list(Pageable pageable, @Nullable Filter filter) {
        return supplierRepository.findAll();
    }

    @Override
    public SupplierCreateRequestDto save(SupplierCreateRequestDto dto) {
        Supplier supplier = supplierMapper.toEntity(dto);
        Supplier savedSupplier = supplierRepository.save(supplier);
        log.info("Supplier saved with ID: {}", savedSupplier.getId());
        return dto;
    }

    @Override
    public void delete(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado con ID: " + id));
        supplier.setActive(false); // Borrado lógico.
        supplierRepository.save(supplier);
        log.info("Supplier deactivated with ID: {}", id);
    }

    @Override
    public Optional<Supplier> getSupplierById(Long id) {
        return supplierRepository.findById(id);
    }

    @Override
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    @Override
    public Supplier update(UpdateSupplierRequestDto dto) {
        Supplier existingSupplier = supplierRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));

        supplierMapper.updateSupplierFromDTO(dto, existingSupplier);
        Supplier updatedSupplier = supplierRepository.save(existingSupplier);

        log.info("Supplier updated with ID: {}", updatedSupplier.getId());
        return updatedSupplier;
    }
}
