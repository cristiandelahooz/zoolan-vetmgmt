package com.wornux.dto.request;

import com.wornux.data.enums.ProductCategory;
import com.wornux.data.enums.ProductUnit;
import com.wornux.data.enums.ProductUsageType;
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
public class ProductUpdateRequestDto {

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String name;

    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    private BigDecimal purchasePrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    private BigDecimal salesPrice;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer availableStock;

    @Min(value = 0, message = "El stock contable no puede ser negativo")
    private Integer accountingStock;

    private int reorderLevel;

    private Long supplierId;

    private ProductCategory category;

    private Long warehouseId;

    private ProductUnit unit;

    private ProductUsageType usageType;
}