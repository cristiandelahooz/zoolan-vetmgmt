package com.wornux.services.report.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * Servicio para generar reportes PDF usando JasperReports. Implementa el patrón Builder para
 * configuración fluida.
 */
@Slf4j
@Builder
public class ReportServiceDatabase implements ReportService<ReportServiceDatabase> {

  private static final String REPORT_PATH = "classpath:./reports/Invoice.jasper";

  private final Map<String, Object> parameters = new HashMap<>();
  private ResourceLoader resourceLoader;
  private String version;

  @Setter private List<Map<String, Object>> productsData;

  @Override
  public ReportServiceDatabase put(String key, Object value) {
    parameters.put(key, value);
    return this;
  }

  /**
   * Configura los datos de productos para el reporte.
   *
   * @param productsData Lista de Maps con los datos de cada producto
   * @return this para encadenamiento fluido
   */
  public ReportServiceDatabase withProductsData(List<Map<String, Object>> productsData) {
    this.productsData = productsData;
    return this;
  }

  @Override
  public byte[] execute() throws ReportErrorException {
    try {
      JasperReport jasperReport = loadJasperReport();
      return generatePdfReport(jasperReport);
    } catch (ReportErrorException ex) {
      throw ex;
    } catch (Exception ex) {
      log.error("Error generating report: {}", ex.getMessage(), ex);
      throw new ReportErrorException("Failed to generate report: " + ex.getMessage(), ex);
    }
  }

  private JasperReport loadJasperReport() throws ReportErrorException {
    try (InputStream resourceStream = loadResourceStream()) {
      JasperReport jasperReport = (JasperReport) JRLoader.loadObject(resourceStream);
      log.info("Jasper report loaded successfully: {}", jasperReport.getName());
      return jasperReport;
    } catch (IOException ex) {
      throw new ReportErrorException("Error loading resource: " + ex.getMessage(), ex);
    } catch (JRException ex) {
      throw new ReportErrorException("Error compiling report: " + ex.getMessage(), ex);
    }
  }

  private InputStream loadResourceStream() throws IOException {
    return resourceLoader.getResource(REPORT_PATH).getInputStream();
  }

  private byte[] generatePdfReport(JasperReport jasperReport) throws ReportErrorException {
    try {
      JasperPrint jasperPrint = fillReportWithData(jasperReport);
      byte[] pdfBytes = exportReportToPdf(jasperPrint);
      validateGeneratedPdf(pdfBytes);
      return pdfBytes;
    } catch (Exception ex) {
      throw new ReportErrorException("Error generating PDF: " + ex.getMessage(), ex);
    }
  }

  private JasperPrint fillReportWithData(JasperReport jasperReport) throws JRException {
    if (productsData != null) {
      log.info("Filling report with {} products", productsData.size());
    } else {
      log.warn("No product data provided, filling report with empty dataset");
    }
    JRBeanCollectionDataSource dataSource =
        new JRBeanCollectionDataSource(productsData != null ? productsData : new ArrayList<>());

    parameters.put("productsDataSource", dataSource);

    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

    log.info(
        "Report filled successfully with {} parameters and {} products",
        parameters.size(),
        productsData != null ? productsData.size() : 0);

    return jasperPrint;
  }

  private byte[] exportReportToPdf(JasperPrint jasperPrint) throws IOException, JRException {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
      return outputStream.toByteArray();
    }
  }

  private void validateGeneratedPdf(byte[] pdfBytes) throws ReportErrorException {
    if (pdfBytes.length == 0) {
      throw new ReportErrorException("Generated PDF is empty");
    }
    log.info("PDF generated successfully, size: {} bytes", pdfBytes.length);
  }
}
