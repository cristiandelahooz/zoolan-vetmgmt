package com.wornux.data.repository;

import com.wornux.data.base.AbstractRepository;
import com.wornux.data.entity.Supplier;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends AbstractRepository<Supplier, Long> {

}
