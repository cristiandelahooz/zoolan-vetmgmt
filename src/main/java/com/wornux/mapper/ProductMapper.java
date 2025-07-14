package com.wornux.mapper;

import com.wornux.data.entity.Product;
import com.wornux.data.entity.Supplier;
import com.wornux.dto.request.ProductCreateRequestDto;
import com.wornux.dto.request.ProductUpdateRequestDto;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductCreateRequestDto dto, Supplier supplier) {
        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .supplier(supplier)
                .category(dto.getCategory())
                .build(); // active = true by default
    }

    public void updateProductFromDTO(ProductUpdateRequestDto dto, Product product, Supplier supplier) {
        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getStock() != null) product.setStock(dto.getStock());
        if (dto.getSupplierId() != null) product.setSupplier(supplier);
        if (dto.getCategory() != null) product.setCategory(dto.getCategory());

    }
}

