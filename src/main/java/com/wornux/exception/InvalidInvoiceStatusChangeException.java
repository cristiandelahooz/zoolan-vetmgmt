package com.wornux.exception;

import com.wornux.data.enums.InvoiceStatus;

public class InvalidInvoiceStatusChangeException extends RuntimeException {

  public InvalidInvoiceStatusChangeException(
      InvoiceStatus currentStatus, InvoiceStatus targetStatus) {
    super(
        String.format(
            "Cannot change invoice status from %s to %s",
            currentStatus.getDisplay(), targetStatus.getDisplay()));
  }

  public InvalidInvoiceStatusChangeException(
      InvoiceStatus currentStatus, InvoiceStatus targetStatus, String reason) {
    super(
        String.format(
            "Cannot change invoice status from %s to %s: %s",
            currentStatus.getDisplay(), targetStatus.getDisplay(), reason));
  }

  public InvalidInvoiceStatusChangeException(String message) {
    super(message);
  }
}
