package com.wornux.dto.request;

import com.wornux.data.enums.OfferingType;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceCreateRequestDto {
  private String name;
  private String description;
  private OfferingType serviceType;
  private BigDecimal price;
}
