package com.wornux.dto.report;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar todos los datos de una factura necesarios para generar el reporte PDF.
 * Incluye información del cliente, productos y totales.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceReportDto {

  private Long invoiceId;
  private LocalDate invoiceDate;
  private String clientEmail;
  private String clientName;
  private String clientAddress;
  private String clientPhone;
  private String totalAmount;
  private String subtotal;
  private String tax;
  private String notes;
  private String salesOrder;

  private List<Map<String, Object>> productsData;
}
