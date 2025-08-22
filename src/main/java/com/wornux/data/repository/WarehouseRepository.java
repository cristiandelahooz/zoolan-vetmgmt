package com.wornux.data.repository;

import com.wornux.data.entity.Warehouse;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Warehouse findByName(String name);

    List<Warehouse> findByStatusTrue();
}
