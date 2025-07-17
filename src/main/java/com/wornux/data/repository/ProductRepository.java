package com.wornux.data.repository;

import com.wornux.data.base.AbstractRepository;
import com.wornux.data.entity.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.wornux.data.enums.ProductCategory;

@Repository
public interface ProductRepository extends AbstractRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategory(ProductCategory category);

    List<Product> findBySupplierId(Long supplierId);

    List<Product> findByActiveTrue();

    @Query("SELECT p FROM Product p WHERE p.stock <= p.reorderLevel AND p.active = true")
    List<Product> findLowStockProducts();

    List<Product> findAllByActiveIsTrue();
}
