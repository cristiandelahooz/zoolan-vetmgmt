package com.wornux.mapper;

import com.wornux.data.entity.Supplier;
import com.wornux.dto.request.SupplierCreateRequestDto;
import com.wornux.dto.request.UpdateSupplierRequestDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SupplierMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "active", constant = "true")
    Supplier toEntity(SupplierCreateRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true) // No permitas cambiar el ID desde el DTO
    @Mapping(target = "products", ignore = true)
    void updateSupplierFromDTO(UpdateSupplierRequestDto dto, @MappingTarget Supplier supplier);

    SupplierCreateRequestDto toDto(Supplier supplier);
}
