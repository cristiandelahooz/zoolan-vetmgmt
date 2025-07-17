package com.wornux.service.interfaces;

import com.vaadin.hilla.BrowserCallable;
import com.wornux.data.entity.Product;
import com.wornux.dto.request.ProductCreateRequestDto;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.wornux.dto.response.ProductListDto;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import com.vaadin.hilla.crud.filter.Filter;
import org.jspecify.annotations.Nullable;

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
     * @return Saved ProductCreateRequestDto (or response DTO).
     */
    ProductCreateRequestDto save(ProductCreateRequestDto dto);

    /**
     * Deactivates (soft delete) a Product.
     *
     * @param id
     *            ID of the Product to delete.
     */
    void delete(Long id);

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
    List<Product> getAllProducts();

    /**
     * Lists paginated active Products for AutoGrid (entities).
     *
     * @param pageable
     *            Pagination parameters.
     * @return Paginated list of active Products.
     */
    List<Product> list(Pageable pageable, @Nullable Filter filter);

    /**
     * Lists paginated active Products as DTOs for frontend.
     *
     * @param pageable
     *            Pagination parameters.
     * @return Paginated list of active ProductListDto.
     */
    List<ProductListDto> listAsDto(Pageable pageable, @Nullable Filter filter);

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
}