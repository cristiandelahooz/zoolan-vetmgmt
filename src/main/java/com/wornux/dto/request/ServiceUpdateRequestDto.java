package com.wornux.dto.request;

import com.wornux.data.enums.OfferingType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceUpdateRequestDto {
  private String name;
  private String description;
  private OfferingType serviceType;
  private Double price;
}
