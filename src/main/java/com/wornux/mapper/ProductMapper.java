package com.wornux.mapper;

import com.wornux.data.entity.Product;
import com.wornux.data.entity.Supplier;
import com.wornux.dto.request.ProductCreateRequestDto;
import com.wornux.dto.request.ProductUpdateRequestDto;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "supplier", source = "supplier")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")

    Product toEntity(ProductCreateRequestDto dto, Supplier supplier);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplier", source = "supplier")
    void updateProductFromDTO(ProductUpdateRequestDto dto, @MappingTarget Product product, Supplier supplier);

    @Mapping(target = "supplierId", source = "supplier.id")
    ProductCreateRequestDto toDTO(Product product);

}
