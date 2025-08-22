package com.wornux.exception;

public class DuplicateClientException extends RuntimeException {
  public DuplicateClientException(String message) {
    super(message);
  }
}
