package com.wornux.data.repository;

import com.wornux.data.entity.Product;
import com.wornux.data.enums.ProductCategory;
import com.wornux.data.enums.ProductUnit;
import com.wornux.data.enums.ProductUsageType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository
        extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategory(ProductCategory category);

    List<Product> findBySupplierId(Long supplierId);

    Page<Product> findByActiveTrue(Pageable pageable);

    List<Product> findByActiveTrue();

    @Query("SELECT p FROM Product p WHERE p.accountingStock <= p.reorderLevel AND p.active = true")
    List<Product> findLowStockProducts();

    List<Product> findAllByActiveIsTrue();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.supplier WHERE p.active = true")
    List<Product> findAllActiveWithSupplier();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.supplier WHERE p.active = true")
    Page<Product> findAllActiveWithSupplier(Pageable pageable);

    List<Product> findByUnitAndActiveTrue(ProductUnit unit);

    List<Product> findByUsageTypeAndActiveTrue(ProductUsageType usageType);

    @Query("SELECT p FROM Product p WHERE p.unit = :unit AND p.usageType = :usageType AND p.active = true")
    List<Product> findByUnitAndUsageTypeAndActiveTrue(@Param("unit") ProductUnit unit,
                                                      @Param("usageType") ProductUsageType usageType);

    @Query("SELECT p FROM Product p WHERE p.warehouse.id = :warehouseId AND p.usageType = :usageType AND p.active = true")
    List<Product> findByWarehouseAndUsageTypeAndActiveTrue(@Param("warehouseId") Long warehouseId,
                                                           @Param("usageType") ProductUsageType usageType);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.supplier LEFT JOIN FETCH p.warehouse " +
            "WHERE (:unit IS NULL OR p.unit = :unit) " +
            "AND (:usageType IS NULL OR p.usageType = :usageType) " +
            "AND (:warehouseId IS NULL OR p.warehouse.id = :warehouseId) " +
            "AND p.active = true")
    Page<Product> findWithFilters(@Param("unit") ProductUnit unit,
                                  @Param("usageType") ProductUsageType usageType,
                                  @Param("warehouseId") Long warehouseId,
                                  Pageable pageable);
}