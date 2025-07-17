package com.wornux.services.implementations;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.FormService;
import com.vaadin.hilla.crud.ListRepositoryService;
import com.vaadin.hilla.crud.filter.Filter;
import com.wornux.data.entity.Product;
import com.wornux.data.entity.Supplier;
import com.wornux.data.repository.ProductRepository;
import com.wornux.data.repository.SupplierRepository;
import com.wornux.dto.request.ProductCreateRequestDto;
import com.wornux.dto.request.ProductUpdateRequestDto;
import com.wornux.exception.ProductNotFoundException;
import com.wornux.exception.SupplierNotFoundException;
import com.wornux.mapper.ProductMapper;
import com.wornux.services.interfaces.ProductService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import com.wornux.data.enums.ProductCategory;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@BrowserCallable
@Transactional
@AnonymousAllowed
public class ProductServiceImpl extends ListRepositoryService<Product, Long, ProductRepository>
        implements ProductService, FormService<ProductCreateRequestDto, Long> {

    @Getter
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Product> list(Pageable pageable, @Nullable Filter filter) {
        log.debug("Listing active Products");
        return productRepository.findByActiveTrue();
    }

    @Override
    @Nullable
    public ProductCreateRequestDto save(ProductCreateRequestDto dto) {
        try {
            Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                    .orElseThrow(() -> new SupplierNotFoundException(dto.getSupplierId()));

            Product product = productMapper.toEntity(dto, supplier);
            Product savedProduct = productRepository.save(product);

            log.info("Product created with ID: {}", savedProduct.getId());
            return dto;  // O puedes mapear la entidad a un DTO de respuesta si prefieres
        } catch (Exception e) {
            log.error("Error creating Product: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Product update(Long id, ProductUpdateRequestDto dto) {
        log.debug("Updating Product ID: {}", id);

        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));

        Supplier supplier = null;
        if (dto.getSupplierId() != null) {
            supplier = supplierRepository.findById(dto.getSupplierId())
                    .orElseThrow(() -> new SupplierNotFoundException(dto.getSupplierId()));
        }

        productMapper.updateProductFromDTO(dto, product, supplier);
        Product updatedProduct = productRepository.save(product);

        log.info("Product updated with ID: {}", updatedProduct.getId());
        return updatedProduct;
    }

    @Override
    public void delete(Long id) {
        log.debug("Deactivating Product ID: {}", id);

        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));

        product.setActive(false);
        productRepository.save(product);

        log.info("Product deactivated ID: {}", id);
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(ProductCategory.valueOf(category));
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsBySupplier(Long supplierId) {
        return productRepository.findBySupplierId(supplierId);
    }

    @Override
    @Transactional(readOnly = true)
    public Product update(Product product) {
        log.debug("Updating Product: {}", product);
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAllByActiveIsTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts() {
        log.debug("Retrieving products with low stock");
        return productRepository.findLowStockProducts();
    }

    @Override
    @Transactional(readOnly = true)
    public long getCount(Specification<Product> specification) {
        return productRepository.count(specification);
    }
}
