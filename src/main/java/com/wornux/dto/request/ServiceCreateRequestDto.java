package com.wornux.dto.request;

import java.math.BigDecimal;

import com.wornux.data.enums.ServiceType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceCreateRequestDto {
  private String name;
  private String description;
  private ServiceType serviceType;
  private BigDecimal price;
}
