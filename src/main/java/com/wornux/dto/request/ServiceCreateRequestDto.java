package com.wornux.dto.request;

import com.wornux.data.enums.OfferingType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ServiceCreateRequestDto {
  private String name;
  private String description;
  private OfferingType offeringType;
  private BigDecimal price;
}
