package com.wornux.services.report.pdf;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
@RequiredArgsConstructor
public class JasperReportFactory {

    private final DataSource dataSource;

    @Value("${application.version:unknown}")
    private String version;

    public ReportService<ReportServiceDatabase> getServiceFromDatabase(String resourcePath) {
        return ReportServiceDatabase.builder().dataSource(dataSource).jasperResource(resourcePath).version(version)
                .build();
    }

}
