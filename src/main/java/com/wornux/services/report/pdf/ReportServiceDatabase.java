package com.wornux.services.report.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Slf4j
@Builder
public class ReportServiceDatabase implements ReportService<ReportServiceDatabase> {

  private final Map<String, Object> parameters = new HashMap<>();
  private ResourceLoader resourceLoader;
  private DataSource dataSource;
  private String version;

  @Override
  public ReportServiceDatabase put(String key, Object value) {
    parameters.put(key, value);
    return this;
  }

  public ReportServiceDatabase putAll(Map<String, Object> values) {
    if (values != null) {
      parameters.putAll(values);
    }
    return this;
  }

  public ReportServiceDatabase clearAll() {
    parameters.clear();
    return this;
  }

  @Override
  public byte[] execute() throws ReportErrorException {
    try (Connection connection = establishDatabaseConnection()) {
      JasperReport jasperReport = loadJasperReport();
      return generatePdfReport(jasperReport, connection);
    } catch (ReportErrorException ex) {
      throw ex;
    } catch (Exception ex) {
      handleUnexpectedError(ex);
      throw new ReportErrorException("Failed to generate report: " + ex.getMessage(), ex);
    }
  }

  private Connection establishDatabaseConnection() throws Exception {
    Connection connection = dataSource.getConnection();
    log.info("Database connection established successfully");
    return connection;
  }

  private JasperReport loadJasperReport() throws Exception {
    try (InputStream resourceStream = loadResourceStream()) {
      JasperReport jasperReport = loadAndValidateJasperReport(resourceStream);
      log.info("Jasper report loaded successfully: {}", jasperReport.getName());
      return jasperReport;
    } catch (IOException ex) {
      throw new ReportErrorException("Error loading resource: " + ex.getMessage(), ex);
    }
  }

  private InputStream loadResourceStream() throws IOException {
    Resource resource = resourceLoader.getResource("classpath:./report/Invoice.jrxml");
    return resource.getInputStream();
  }

  private JasperReport loadAndValidateJasperReport(InputStream resourceStream)
      throws ReportErrorException, JRException {
    return JasperCompileManager.compileReport(resourceStream);
  }

  private byte[] generatePdfReport(JasperReport jasperReport, Connection connection)
      throws Exception {
    JasperPrint jasperPrint = fillReportWithData(jasperReport, connection);
    byte[] pdfBytes = exportReportToPdf(jasperPrint);
    validateGeneratedPdf(pdfBytes);
    return pdfBytes;
  }

  private JasperPrint fillReportWithData(JasperReport jasperReport, Connection connection)
      throws Exception {
    //TODO: Remover prueba de data
    parameters.put("orderId", "1");
    parameters.put("orderDate", "20/10/2025");

    // User information
    parameters.put("userEmail", "cristiandelahooz@gmail.com");

    // Prepare book data for the report
    List<Map<String, Object>> bookData = List.of(
        new HashMap<>() {{
          put("title", "title");
          put("author", "author");
          put("price", 15.3);
          put("quantity", 1); // Quantity is always 1
          put("totalPrice", 20.0); // Price * Quantity
        }}
    );
    // Create a JRBeanCollectionDataSource from the list
    JRBeanCollectionDataSource bookDataSource = new JRBeanCollectionDataSource(bookData);

    // Pass the books datasource as a parameter
    parameters.put("bookDataSource", bookDataSource);

    // Total Invoice (sum of all book prices)
    double totalInvoice = 202.0;
    parameters.put("totalInvoice", String.format("%.2f", totalInvoice));
    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters,
        bookDataSource);
    log.info("Report filled successfully with {} parameters", parameters.size());
    return jasperPrint;
  }

  private byte[] exportReportToPdf(JasperPrint jasperPrint) throws IOException, JRException {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
      return outputStream.toByteArray();
    }
  }

  private void validateGeneratedPdf(byte[] pdfBytes) {
    if (pdfBytes.length == 0) {
      throw new ReportErrorException("Generated PDF is empty");
    }
    log.info("PDF generated successfully, size: {} bytes", pdfBytes.length);
  }

  private void handleUnexpectedError(Exception ex) {
    log.error("Error generating report: {}", ex.getMessage(), ex);
  }
}
