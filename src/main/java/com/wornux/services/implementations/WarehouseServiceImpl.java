package com.wornux.services.implementations;

import static org.reflections.Reflections.log;

import com.wornux.data.entity.Warehouse;
import com.wornux.data.repository.WarehouseRepository;
import com.wornux.dto.request.WarehouseCreateRequestDto;
import com.wornux.dto.request.WarehouseUpdateRequestDto;
import com.wornux.dto.response.WarehouseResponseDto;
import com.wornux.exception.WarehouseNotFoundException;
import com.wornux.mapper.WarehouseMapper;
import com.wornux.services.interfaces.WarehouseService;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;

    @Override
    public WarehouseResponseDto createWarehouse(WarehouseCreateRequestDto createDTO) {
        Warehouse warehouse = warehouseMapper.toEntity(createDTO);
        Warehouse saved = warehouseRepository.save(warehouse);
        return warehouseMapper.toResponseDto(saved);
    }

    @Override
    public Warehouse updateWarehouse(Long id, WarehouseUpdateRequestDto updateDTO) {
        Warehouse warehouse = warehouseRepository.findById(id).orElseThrow(() -> new WarehouseNotFoundException(id));
        if (updateDTO.getName() != null)
            warehouse.setName(updateDTO.getName());
        if (updateDTO.getWarehouseType() != null)
            warehouse.setWarehouseType(updateDTO.getWarehouseType());
        if (updateDTO.getStatus() != null)
            warehouse.setStatus(updateDTO.getStatus());
        if (updateDTO.getAvailableForSale() != null)
            warehouse.setAvailableForSale(updateDTO.getAvailableForSale());
        if (updateDTO.getProducts() != null)
            warehouse.setProducts(updateDTO.getProducts());
        return warehouseRepository.save(warehouse);
    }

    @Override
    public WarehouseResponseDto getWarehouseById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id).orElseThrow(() -> new WarehouseNotFoundException(id));
        return warehouseMapper.toResponseDto(warehouse);
    }

    @Override
    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findByStatusTrue();
    }

    @Override
    @Transactional
    public void deleteWarehouse(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id).orElseThrow(() -> new WarehouseNotFoundException(id));

        if (warehouse.getProducts() != null && !warehouse.getProducts().isEmpty()) {
            // Opcional: validar si tiene productos activos
            log.info("Warehouse has {} products, performing soft delete", warehouse.getProducts().size());
        }

        warehouse.setStatus(false);
        warehouseRepository.save(warehouse);
    }
}
