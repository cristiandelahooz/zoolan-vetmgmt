package com.wornux.services.implementations;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.FormService;
import com.vaadin.hilla.crud.ListRepositoryService;
import com.vaadin.hilla.crud.filter.Filter;
import com.wornux.data.entity.Supplier;
import com.wornux.data.repository.SupplierRepository;
import com.wornux.dto.request.SupplierCreateRequestDto;
import com.wornux.dto.request.UpdateSupplierRequestDto;
import com.wornux.dto.response.SupplierListDto;
import com.wornux.mapper.SupplierMapper;
import com.wornux.services.interfaces.SupplierService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@BrowserCallable
@AnonymousAllowed
@Transactional
public class SupplierServiceImpl extends ListRepositoryService<Supplier, Long, SupplierRepository> implements
        SupplierService, FormService<SupplierCreateRequestDto, Long> {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Supplier> list(Pageable pageable, @Nullable Filter filter) {
        log.debug("Listing active Suppliers with pagination");
        return supplierRepository.findByActiveTrue(pageable).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierListDto> listAsDto(Pageable pageable, @Nullable Filter filter) {
        log.debug("Listing active Suppliers as DTOs with pagination");
        List<Supplier> suppliers = supplierRepository.findByActiveTrue(pageable).getContent();
        return supplierMapper.toListDtoList(suppliers);
    }

    @Override
    public SupplierCreateRequestDto save(SupplierCreateRequestDto dto) {
        Supplier supplier = supplierMapper.toEntity(dto);
        Supplier savedSupplier = supplierRepository.save(supplier);
        log.info("Supplier saved with ID: {}", savedSupplier.getId());
        return supplierMapper.toDto(savedSupplier);
    }

    @Override
    public void delete(Long id) {
        Supplier supplier = supplierRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(
                "Proveedor no encontrado con ID: " + id));
        supplier.setActive(false);
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
        Supplier existingSupplier = supplierRepository.findById(dto.getId()).orElseThrow(
                () -> new IllegalArgumentException("Proveedor no encontrado"));

        supplierMapper.updateSupplierFromDTO(dto, existingSupplier);
        Supplier updatedSupplier = supplierRepository.save(existingSupplier);

        log.info("Supplier updated with ID: {}", updatedSupplier.getId());
        return updatedSupplier;
    }

    @Override
    public SupplierRepository getRepository() {
        return supplierRepository;
    }

    @Override
    public SupplierCreateRequestDto getCreateDtoById(Long id) {
        Supplier supplier = getRepository().findById(id).orElseThrow(() -> new EntityNotFoundException(
                "Proveedor no encontrado"));

        return supplierMapper.toDto(supplier);
    }
}
