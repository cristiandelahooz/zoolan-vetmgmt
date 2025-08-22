package com.wornux.services.report.pdf;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

class ReportServiceDatabaseTest {

    private ResourceLoader resourceLoader;
    private ReportServiceDatabase reportService;

    @BeforeEach
    void setUp() {
        resourceLoader = mock(ResourceLoader.class);
        reportService = ReportServiceDatabase.builder().resourceLoader(resourceLoader).version("1.0").build();
    }

    @Test
    void execute_shouldThrowException_whenResourceNotFound() throws Exception {
        Resource resource = mock(Resource.class);
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        when(resource.getInputStream()).thenThrow(new RuntimeException("Resource not found"));

        assertThrows(ReportErrorException.class, () -> reportService.execute());
    }

    @Test
    void put_shouldAddParameter() {
        ReportServiceDatabase result = reportService.put("key", "value");

        assertNotNull(result);
    }

    @Test
    void execute_shouldThrowException_whenInvalidJasperFile() throws Exception {
        Resource resource = mock(Resource.class);
        InputStream invalidStream = new ByteArrayInputStream("invalid content".getBytes());

        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        when(resource.getInputStream()).thenReturn(invalidStream);

        assertThrows(ReportErrorException.class, () -> reportService.execute());
    }
}
