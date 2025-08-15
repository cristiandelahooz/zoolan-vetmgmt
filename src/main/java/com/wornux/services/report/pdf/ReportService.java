package com.wornux.services.report.pdf;

import java.io.InputStream;

public interface ReportService<T> {

  byte[] execute() throws ReportErrorException;

  T put(String key, Object value);
}
