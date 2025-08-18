package com.wornux.exception;

public class InvoiceNotFoundException extends RuntimeException {
  public InvoiceNotFoundException(Long invoiceId) {
    super("Invoice not found with id: " + invoiceId);
  }
}
