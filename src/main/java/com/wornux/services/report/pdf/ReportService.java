package com.wornux.services.report.pdf;

import java.io.InputStream;

/**
 * @author me@fredpena.dev
 * @created 26/04/2024 - 00:23
 */
public interface ReportService<T> {

    InputStream execute() throws ReportErrorException;

    T put(String key, Object value);
}
