package com.wornux.services.interfaces;

import com.wornux.services.validation.ValidationErrorContext;
import jakarta.validation.ConstraintViolationException;

/**
 * Central service for handling validation errors across the application. Provides consistent error
 * handling with context-aware customization.
 *
 * <p>This service follows the strategy pattern to allow different error handling strategies based
 * on the UI context while maintaining consistency in error processing and user message formatting.
 *
 * @author Veterinary Management System
 * @since 1.0.0
 */
public interface ValidationErrorHandler {

  /**
   * Handles constraint violation exceptions with the provided context.
   *
   * @param exception the constraint violation exception to handle
   * @param context the context containing configuration for error handling
   */
  void handleValidationErrors(
      ConstraintViolationException exception, ValidationErrorContext context);

  /**
   * Handles constraint violation exceptions with default context settings.
   *
   * @param exception the constraint violation exception to handle
   */
  void handleValidationErrors(ConstraintViolationException exception);

  /**
   * Formats a single validation message to be user-friendly and professional.
   *
   * @param originalMessage the original validation message
   * @return formatted user-friendly message
   */
  String formatValidationMessage(String originalMessage);
}
