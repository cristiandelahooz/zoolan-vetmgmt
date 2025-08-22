package com.wornux.mapper;

import com.wornux.data.entity.Product;
import com.wornux.data.entity.Supplier;
import com.wornux.dto.request.ProductCreateRequestDto;
import com.wornux.dto.request.ProductUpdateRequestDto;
import com.wornux.dto.response.ProductListDto;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "active", ignore = true)
  @Mapping(target = "supplier", source = "supplier")
  Product toEntity(ProductCreateRequestDto dto, Supplier supplier);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "supplier", source = "supplier")
  void partialUpdate(
      @MappingTarget Product product, ProductUpdateRequestDto dto, Supplier supplier);

  ProductCreateRequestDto toCreateDto(Product product);

  @Mapping(target = "supplierName", source = "supplier.companyName")
  ProductListDto toListDto(Product product);

  List<ProductListDto> toListDtoList(List<Product> products);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "supplier", source = "supplier")
  void updateProductFromDTO(
      ProductUpdateRequestDto dto, @MappingTarget Product product, Supplier supplier);

  @Mapping(target = "supplierId", source = "supplier.id")
  ProductCreateRequestDto toDTO(Product product);
}
