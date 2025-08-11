package com.wornux.dto.response;

import com.wornux.data.entity.Product;
import com.wornux.data.enums.WarehouseType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WarehouseResponseDto {
    private Long id;
    private String name;
    private WarehouseType warehouseType;
    private boolean status;
    private boolean availableForSale;
    private List<Product> products;
}