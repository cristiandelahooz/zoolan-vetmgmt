package com.wornux.mapper;

import com.wornux.data.entity.Warehouse;
import com.wornux.dto.request.WarehouseCreateRequestDto;
import com.wornux.dto.response.WarehouseResponseDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WarehouseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "warehouseType", source = "warehouseType")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "availableForSale", source = "availableForSale")
    @Mapping(target = "products", source = "products")
    Warehouse toEntity(WarehouseCreateRequestDto dto);

    @Mapping(target = "name", source = "name")
    @Mapping(target = "warehouseType", source = "warehouseType")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "availableForSale", source = "availableForSale")
    @Mapping(target = "products", source = "products")
    WarehouseCreateRequestDto toDto(Warehouse entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "warehouseType", source = "warehouseType")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "availableForSale", source = "availableForSale")
    @Mapping(target = "products", ignore = true)
    WarehouseResponseDto toResponseDto(Warehouse entity);
}