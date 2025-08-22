package com.wornux.dto.request;

import static com.wornux.constants.ValidationConstants.MAX_WAREHOUSE_NAME_LENGTH;

import com.wornux.data.entity.Product;
import com.wornux.data.enums.WarehouseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@Builder
public class WarehouseUpdateRequestDto {
    @NotBlank(message = "El nombre del almacén es obligatorio")
    @Size(min = 1, max = MAX_WAREHOUSE_NAME_LENGTH, message = "El nombre del almacén debe tener entre {min} y {max} caracteres")
    private String name;

    @NotNull(message = "El tipo de almacén es obligatorio")
    private WarehouseType warehouseType;

    @Nullable
    private Boolean status;

    @Nullable
    private Boolean availableForSale;

    @Nullable
    private List<Product> products;
}
