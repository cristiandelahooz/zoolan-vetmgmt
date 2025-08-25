package com.wornux.utils;

import com.wornux.services.interfaces.ValidationErrorHandler;
import com.wornux.services.validation.ValidationErrorContext;
import jakarta.validation.ConstraintViolationException;
import lombok.experimental.UtilityClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Utility class that provides static access to validation error handling functionality. Acts as a
 * bridge between static utility usage and the Spring-managed validation services.
 *
 * <p>This utility maintains backward compatibility while providing access to the new centralized
 * validation error handling system.
 *
 * @author Veterinary Management System
 * @since 1.0.0
 */
@Component
public class ValidationNotificationUtils implements ApplicationContextAware {

  private static ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(@NonNull ApplicationContext context) {
    ValidationNotificationUtils.applicationContext = context;
  }

  /**
   * Handles validation errors using the default context.
   *
   * @param exception the constraint violation exception to handle
   */
  public static void handleValidationErrors(ConstraintViolationException exception) {
    getValidationErrorHandler().handleValidationErrors(exception);
  }

  /**
   * Handles validation errors with a custom context.
   *
   * @param exception the constraint violation exception to handle
   * @param context the context configuration for error display
   */
  public static void handleValidationErrors(
      ConstraintViolationException exception, ValidationErrorContext context) {
    getValidationErrorHandler().handleValidationErrors(exception, context);
  }

  /**
   * Handles validation errors optimized for calendar operations.
   *
   * @param exception the constraint violation exception to handle
   */
  public static void handleCalendarValidationErrors(ConstraintViolationException exception) {
    getValidationErrorHandler()
        .handleValidationErrors(exception, ValidationErrorContext.forCalendarOperations());
  }

  /**
   * Handles validation errors optimized for dialog forms.
   *
   * @param exception the constraint violation exception to handle
   */
  public static void handleDialogValidationErrors(ConstraintViolationException exception) {
    getValidationErrorHandler()
        .handleValidationErrors(exception, ValidationErrorContext.forDialogForms());
  }

  /**
   * Handles validation errors optimized for general forms.
   *
   * @param exception the constraint violation exception to handle
   */
  public static void handleFormValidationErrors(ConstraintViolationException exception) {
    getValidationErrorHandler()
        .handleValidationErrors(exception, ValidationErrorContext.forIndividualErrorsImmediate());
  }

  /**
   * Handles validation errors with immediate individual notifications. Ideal for cases requiring
   * instant feedback without staggered timing.
   *
   * @param exception the constraint violation exception to handle
   */
  public static void handleIndividualValidationErrors(ConstraintViolationException exception) {
    getValidationErrorHandler()
        .handleValidationErrors(exception, ValidationErrorContext.forIndividualErrorsImmediate());
  }

  /**
   * Formats a validation message using the centralized formatter.
   *
   * @param originalMessage the original validation message
   * @return formatted user-friendly message
   */
  public static String formatValidationMessage(String originalMessage) {
    return getValidationErrorHandler().formatValidationMessage(originalMessage);
  }

  /** Gets the ValidationErrorHandler bean from the application context. */
  private static ValidationErrorHandler getValidationErrorHandler() {
    if (applicationContext == null) {
      throw new IllegalStateException(
          "ApplicationContext not initialized. Ensure ValidationNotificationUtils is properly"
              + " configured as a Spring bean.");
    }

    return applicationContext.getBean(ValidationErrorHandler.class);
  }

  /** Utility class for common validation error context builders. */
  @UtilityClass
  public static class ContextBuilder {

    /** Creates a context for high-priority errors that need immediate attention. */
    public static ValidationErrorContext critical() {
      return ValidationErrorContext.builder()
          .position(com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER)
          .duration(10000)
          .combineErrors(true)
          .additionalCssClasses("critical-validation-error")
          .build();
    }

    /** Creates a context for subtle notifications that don't interrupt workflow. */
    public static ValidationErrorContext subtle() {
      return ValidationErrorContext.builder()
          .position(com.vaadin.flow.component.notification.Notification.Position.BOTTOM_END)
          .duration(4000)
          .combineErrors(true)
          .additionalCssClasses("subtle-validation-error")
          .build();
    }

    /** Creates a context for mobile-optimized notifications. */
    public static ValidationErrorContext mobile() {
      return ValidationErrorContext.builder()
          .position(com.vaadin.flow.component.notification.Notification.Position.TOP_STRETCH)
          .duration(6000)
          .combineErrors(true)
          .additionalCssClasses("mobile-validation-error")
          .build();
    }
  }
}
