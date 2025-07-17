package com.wornux.dto.response;

import com.wornux.data.enums.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO usado para enviar información básica de un producto al frontend en operaciones de listado o consulta.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListDto {

    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private int stock;

    private ProductCategory category;

    private String supplierName;
}
