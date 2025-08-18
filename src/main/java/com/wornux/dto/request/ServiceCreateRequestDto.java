package com.wornux.dto.request;

import com.wornux.data.enums.ServiceCategory;
import com.wornux.data.enums.ServiceType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ServiceCreateRequestDto {
  private String name;
  private String description;
  private ServiceCategory serviceCategory;
  private BigDecimal price;
}