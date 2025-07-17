package com.wornux.data.repository;

import com.wornux.data.base.AbstractRepository;
import com.wornux.data.entity.Supplier;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface SupplierRepository extends AbstractRepository<Supplier, Long> {

    @Query("SELECT s FROM Supplier s WHERE s.active = true")
    Page<Supplier> findByActiveTrue(Pageable pageable);

}
