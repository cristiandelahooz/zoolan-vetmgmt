package com.wornux.services.report.pdf;

import lombok.Builder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Builder
public class ReportServiceDatabase implements ReportService<ReportServiceDatabase> {

    private final Map<String, Object> parameters = new HashMap<>();
    @Setter
    private String jasperResource;
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

    public ReportServiceDatabase version(String version) {
        this.version = version;
        return this;
    }

    @Override
    public InputStream execute() throws ReportErrorException {
        validateJasperResource();
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

    private void validateJasperResource() {
        if (jasperResource == null || jasperResource.trim().isEmpty()) {
            throw new ReportErrorException("Jasper resource path cannot be null or empty");
        }
        log.info("Processing jasper resource: {}", jasperResource);
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
        }
    }

    private InputStream loadResourceStream() {
        InputStream resourceStream = ReportServiceDatabase.class.getResourceAsStream(jasperResource);
        if (resourceStream == null) {
            throw new ReportErrorException("Jasper resource not found: " + jasperResource);
        }
        return resourceStream;
    }

    private JasperReport loadAndValidateJasperReport(InputStream resourceStream)
            throws ReportErrorException, JRException {
        Object loadedObject = JRLoader.loadObject(resourceStream);
        if (!(loadedObject instanceof JasperReport jasperReport)) {
            String actualType = loadedObject != null ? loadedObject.getClass().getSimpleName() : "null";
            throw new ReportErrorException("Invalid jasper file format. Expected JasperReport but got: " + actualType);
        }
        return jasperReport;
    }

    private InputStream generatePdfReport(JasperReport jasperReport, Connection connection) throws Exception {
        JasperPrint jasperPrint = fillReportWithData(jasperReport, connection);
        byte[] pdfBytes = exportReportToPdf(jasperPrint);
        validateGeneratedPdf(pdfBytes);
        return new ByteArrayInputStream(pdfBytes);
    }

    private JasperPrint fillReportWithData(JasperReport jasperReport, Connection connection) throws Exception {
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
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