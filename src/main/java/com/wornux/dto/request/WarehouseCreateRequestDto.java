package com.wornux.dto.request;

import static com.wornux.constants.ValidationConstants.MAX_WAREHOUSE_NAME_LENGTH;

import com.wornux.data.entity.Product;
import com.wornux.data.enums.WarehouseType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseCreateRequestDto {

  @NotBlank(message = "El nombre del almacén es obligatorio")
  @Size(
      min = 1,
      max = MAX_WAREHOUSE_NAME_LENGTH,
      message = "El nombre del almacén debe tener entre {min} y {max} caracteres")
  private String name;

  @NotNull(message = "El tipo de almacén es obligatorio")
  private WarehouseType warehouseType;

  @Builder.Default private boolean status = true;

  @NotNull private boolean availableForSale;

  @Builder.Default private List<Product> products = new ArrayList<>();
}
