package com.wornux.dto.dashboard;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueDataDto {
  private LocalDate period;
  private Double revenue;
  private Long invoiceCount;
  private Boolean isPrediction;
}
