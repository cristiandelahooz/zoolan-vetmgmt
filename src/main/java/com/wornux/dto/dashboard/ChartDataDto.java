package com.wornux.dto.dashboard;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChartDataDto {
  private String label;
  private Double value;
  private LocalDate date;
  private String category;
}
