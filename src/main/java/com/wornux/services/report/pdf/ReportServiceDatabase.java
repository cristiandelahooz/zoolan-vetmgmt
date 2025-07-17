package com.wornux.services.report.pdf;

import lombok.Builder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author hectorvent@gmail.com
 */
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

        try (Connection connection = dataSource.getConnection()) {
            log.info("jasperResource: {}", jasperResource);
            InputStream resourceAsStream = ReportServiceDatabase.class.getResourceAsStream(jasperResource);
            log.info("resourceAsStream: {}", resourceAsStream);

            JasperReport jasperFile = (JasperReport) JRLoader.loadObject(resourceAsStream);
            log.info("jasperFile: {}", jasperFile);

            Objects.requireNonNull(jasperFile, "The Jasper file wan not found!!");

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperFile, parameters, connection);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            JasperExportManager.exportReportToPdfStream(jasperPrint, byteArrayOutputStream);

            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        } catch (Exception ex) {
            throw new ReportErrorException(ex);
        }
    }

}
