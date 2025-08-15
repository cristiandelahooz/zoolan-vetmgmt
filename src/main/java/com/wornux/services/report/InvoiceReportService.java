package com.wornux.services.report;

import com.wornux.data.entity.Invoice;
import com.wornux.dto.report.InvoiceReportDto;
import com.wornux.mapper.InvoiceReportMapper;
import com.wornux.services.report.pdf.JasperReportFactory;
import com.wornux.services.report.pdf.ReportErrorException;
import com.wornux.services.report.pdf.ReportService;
import com.wornux.services.report.pdf.ReportServiceDatabase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Servicio para generar reportes de facturas en formato PDF. Utiliza MapStruct para mapear
 * entidades a DTOs y JasperReports para generar PDFs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceReportService {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private final InvoiceReportMapper invoiceReportMapper;
  private final JasperReportFactory reportFactory;

  /**
   * Prepara el servicio de reportes con todos los datos de la factura.
   *
   * @param invoice La factura a procesar
   * @return ReportService configurado con todos los parámetros necesarios
   */
  public ReportServiceDatabase prepareInvoiceReport(Invoice invoice) {
    log.debug("Preparando reporte para factura #{}", invoice.getCode());

    InvoiceReportDto reportDto = invoiceReportMapper.toReportDto(invoice);

    ReportServiceDatabase reportService = (ReportServiceDatabase) reportFactory.getServiceFromDatabase();

    return reportService
        .withProductsData(reportDto.getProductsData())
        .put("invoiceId", String.valueOf(reportDto.getInvoiceId()))
        .put("invoiceDate", reportDto.getInvoiceDate() != null
            ? reportDto.getInvoiceDate().format(DATE_FORMAT) : "")
        .put("clientEmail", reportDto.getClientEmail() != null ? reportDto.getClientEmail() : "")
        .put("clientName", reportDto.getClientName() != null ? reportDto.getClientName() : "")
        .put("clientAddress",
            reportDto.getClientAddress() != null ? reportDto.getClientAddress() : "")
        .put("totalInvoice",
            reportDto.getTotalAmount() != null ? reportDto.getTotalAmount() : "0.00")
        .put("subtotal", reportDto.getSubtotal() != null ? reportDto.getSubtotal() : "0.00")
        .put("tax", reportDto.getTax() != null ? reportDto.getTax() : "0.00")
        .put("salesOrder", reportDto.getSalesOrder() != null ? reportDto.getSalesOrder() : "");
  }

  /**
   * Genera el PDF de la factura.
   *
   * @param invoice La factura a generar
   * @return byte array del PDF generado
   */
  public byte[] generateInvoicePdf(Invoice invoice) {
    log.info("Generando PDF para factura #{}", invoice.getCode());

    try {
      ReportServiceDatabase reportService = prepareInvoiceReport(invoice);
      byte[] pdfData = reportService.execute();

      log.info("PDF generado exitosamente para factura #{}, tamaño: {} bytes",
          invoice.getCode(), pdfData.length);

      return pdfData;
    } catch (Exception e) {
      log.error("Error generando PDF para factura #{}: {}", invoice.getCode(), e.getMessage(), e);
      throw new ReportErrorException("Error al generar el PDF de la factura", e);
    }
  }
}