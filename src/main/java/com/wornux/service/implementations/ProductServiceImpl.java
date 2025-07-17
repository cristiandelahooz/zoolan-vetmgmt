package com.wornux.service.implementations;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.CrudRepositoryService;
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
import com.wornux.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.wornux.data.enums.ProductCategory;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@BrowserCallable
@AnonymousAllowed
@Transactional
public class ProductServiceImpl extends CrudRepositoryService<Product, Long, ProductRepository>
        implements ProductService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final ProductMapper productMapper;

    @Override
    @Nullable
    public Product save(ProductCreateRequestDto dto) {
        try {
            Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                    .orElseThrow(() -> new SupplierNotFoundException(dto.getSupplierId()));

            Product product = productMapper.toEntity(dto, supplier);
            Product savedProduct = productRepository.save(product);

            log.info("Product created with ID: {}", savedProduct.getId());
            return savedProduct; // Retornar la entidad guardada
        } catch (Exception e) {
            log.error("Error creating Product: {}", e.getMessage());
            throw e;
        }
    }

    public Product createProduct(ProductCreateRequestDto dto) {
        try {
            Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                    .orElseThrow(() -> new SupplierNotFoundException(dto.getSupplierId()));

            Product product = productMapper.toEntity(dto, supplier);
            Product savedProduct = productRepository.save(product);

            log.info("Product created with ID: {}", savedProduct.getId());
            return savedProduct;
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

    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts() {
        log.debug("Retrieving products with low stock");
        return productRepository.findLowStockProducts();
    }

    public Product updateStock(Long productId, int newStock) {
        log.debug("Updating stock for Product ID: {} to {}", productId, newStock);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setStock(newStock);
        Product updatedProduct = productRepository.save(product);

        log.info("Stock updated for Product ID: {}", productId);
        return updatedProduct;
    }
}