package com.wornux.data.repository;

import com.wornux.data.base.AbstractRepository;
import com.wornux.data.entity.Product;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.wornux.data.enums.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ProductRepository extends AbstractRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategory(ProductCategory category);

    List<Product> findBySupplierId(Long supplierId);

    Page<Product> findByActiveTrue(Pageable pageable);

}
