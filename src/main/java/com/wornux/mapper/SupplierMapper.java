package com.wornux.mapper;

import com.wornux.data.entity.Supplier;
import com.wornux.dto.request.SupplierCreateRequestDto;
import com.wornux.dto.request.UpdateSupplierRequestDto;
import com.wornux.dto.response.SupplierListDto;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SupplierMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    Supplier toEntity(SupplierCreateRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateSupplierFromDTO(UpdateSupplierRequestDto dto, @MappingTarget Supplier supplier);

    SupplierCreateRequestDto toDto(Supplier supplier);

    SupplierListDto toListDto(Supplier supplier);

    List<SupplierListDto> toListDtoList(List<Supplier> suppliers);
}
