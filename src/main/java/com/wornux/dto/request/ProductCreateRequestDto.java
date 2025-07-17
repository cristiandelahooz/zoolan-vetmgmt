package com.wornux.dto.request;

import com.wornux.data.enums.ProductCategory;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequestDto {

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String name;

    private String description;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    private BigDecimal price;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private int stock;

    @NotNull(message = "El proveedor es obligatorio")
    private Long supplierId;

    @NotNull(message = "La categoría del producto es obligatoria")
    private ProductCategory category;

}
