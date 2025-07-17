package com.wornux.service.interfaces;

import com.vaadin.hilla.BrowserCallable;
import com.wornux.data.entity.Product;
import com.wornux.dto.request.ProductCreateRequestDto;
import com.wornux.dto.request.ProductUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link Product} entities.
 */
@BrowserCallable
@Validated
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ProductService {

    /**
     * Saves (creates) a new Product.
     *
     * @param dto
     *            Product creation DTO.
     * @return Saved Product entity.
     */
    Product save(ProductCreateRequestDto dto);

    /**
     * Deactivates (soft delete) a Product.
     *
     * @param id
     *            ID of the Product to delete.
     */
    void delete(Long id);

    /**
     * Updates an existing Product.
     *
     * @param id
     *            ID of the Product to update.
     * @param dto
     *            Product update DTO.
     * @return Updated Product entity.
     */
    Product update(Long id, ProductUpdateRequestDto dto);

    /**
     * Retrieves a Product by its ID.
     *
     * @param id
     *            ID of the Product.
     * @return Optional Product entity.
     */
    Optional<Product> getProductById(Long id);

    /**
     * Lists all active Products.
     *
     * @return List of active Products.
     */
    Page<Product> getAllProducts(Pageable pageable);

    /**
     * Lists Products by supplier.
     *
     * @param supplierId
     *            ID of the supplier.
     * @return List of Products.
     */
    List<Product> getProductsBySupplier(Long supplierId);

    /**
     * Lists Products by category.
     *
     * @param category
     *            Product category name.
     * @return List of Products.
     */
    List<Product> getProductsByCategory(String category);

    /**
     * Lists Products matching a name search.
     *
     * @param name
     *            Name search term.
     * @return List of Products.
     */
    List<Product> getProductsByName(String name);

    /**
     * Lists Products with low stock levels.
     *
     * @return List of Products with stock <= reorderLevel.
     */
    List<Product> getLowStockProducts();
}
