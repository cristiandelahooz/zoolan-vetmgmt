package com.wornux.services.report.pdf;

public class ReportErrorException extends RuntimeException {

  public ReportErrorException(String message) {
    super(message);
  }

  public ReportErrorException(String message, Throwable cause) {
    super(message, cause);
  }

  public ReportErrorException(Throwable cause) {
    super(cause);
  }
}
