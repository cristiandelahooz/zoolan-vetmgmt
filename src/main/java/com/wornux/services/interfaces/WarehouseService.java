package com.wornux.services.interfaces;

import com.wornux.data.entity.Warehouse;
import com.wornux.dto.request.WarehouseCreateRequestDto;
import com.wornux.dto.request.WarehouseUpdateRequestDto;
import com.wornux.dto.response.WarehouseResponseDto;

import java.util.List;

public interface WarehouseService {

    WarehouseResponseDto createWarehouse(WarehouseCreateRequestDto createDTO);
    Warehouse updateWarehouse(Long id, WarehouseUpdateRequestDto updateDTO);
    WarehouseResponseDto getWarehouseById(Long id);
    List<Warehouse> getAllWarehouses();
    void deleteWarehouse(Long id);
}
