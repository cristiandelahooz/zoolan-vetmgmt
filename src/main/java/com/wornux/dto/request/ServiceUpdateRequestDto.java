package com.wornux.dto.request;

import com.wornux.data.enums.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceUpdateRequestDto {
  private String name;
  private String description;
  private ServiceType serviceType;
  private Double price;
}
