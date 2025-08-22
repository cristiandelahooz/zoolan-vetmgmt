package com.wornux.services.report.pdf;

public interface ReportService<T> {

    byte[] execute() throws ReportErrorException;

    T put(String key, Object value);
}
