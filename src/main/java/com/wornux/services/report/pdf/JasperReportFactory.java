package com.wornux.services.report.pdf;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

/**
 * @author hectorvent@gmail.com
 */
@Service
@RequiredArgsConstructor
public class JasperReportFactory {

    private final DataSource dataSource;

    //    private final SystemConfigService systemConfigService;

    @Value("${application.version:unknown}")
    private String version;

    public ReportService<ReportServiceDatabase> getServiceFromDatabase() {
        return ReportServiceDatabase.builder().dataSource(dataSource)
                //                .systemConfigService(systemConfigService)
                .version(version).build();
    }

    public ReportService<ReportServiceDatabase> getServiceFromDatabase(String resourcePath) {
        return ReportServiceDatabase.builder().dataSource(dataSource)
                //                .systemConfigService(systemConfigService)
                .jasperResource(resourcePath).version(version).build();
    }

}
