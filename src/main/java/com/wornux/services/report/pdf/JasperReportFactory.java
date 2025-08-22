package com.wornux.services.report.pdf;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JasperReportFactory {

    private final ResourceLoader resourceLoader;

    @Value("${application.version:unknown}")
    private String version;

    public ReportService<ReportServiceDatabase> getServiceFromDatabase() {
        return ReportServiceDatabase.builder().resourceLoader(resourceLoader).version(version).build();
    }
}
