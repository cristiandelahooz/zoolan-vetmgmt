package com.wornux.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockAlertDto {
  private Long productId;
  private String productName;
  private Integer currentStock;
  private Integer minimumStock;
  private String category;
  private String alertLevel; // LOW, CRITICAL, OUT_OF_STOCK
}