package com.wornux.services.validation;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import lombok.Builder;
import lombok.Value;

/**
 * Context configuration for validation error handling. Contains settings that control how
 * validation errors are displayed to the user.
 *
 * <p>This class uses the builder pattern to provide flexible configuration while maintaining
 * sensible defaults for common use cases.
 *
 * @author Veterinary Management System
 * @since 1.0.0
 */
@Value
@Builder
public class ValidationErrorContext {

  /** Position where notifications should be displayed. */
  @Builder.Default Notification.Position position = Notification.Position.TOP_CENTER;

  /** Duration in milliseconds for each notification. */
  @Builder.Default int duration = 6000;

  /** Notification theme variant for styling. */
  @Builder.Default NotificationVariant themeVariant = NotificationVariant.LUMO_ERROR;

  /** Whether to show notifications with staggered timing to prevent overlap. */
  @Builder.Default boolean staggeredTiming = false;

  /** Delay between staggered notifications in milliseconds. */
  @Builder.Default int staggerDelayMs = 300;

  /** Whether to combine multiple validation errors into a single notification. */
  @Builder.Default boolean combineErrors = true;

  /** Maximum number of individual errors to show (when not combining). */
  @Builder.Default int maxIndividualErrors = 5;

  /** Additional CSS class names to apply to the notification. */
  @Builder.Default String additionalCssClasses = "";

  /** Whether to include technical details in error messages (for development). */
  @Builder.Default boolean includeTechnicalDetails = false;

  /** Creates a context optimized for calendar drag/drop operations. */
  public static ValidationErrorContext forCalendarOperations() {
    return ValidationErrorContext.builder()
        .position(Notification.Position.TOP_CENTER)
        .duration(8000)
        .combineErrors(true)
        .additionalCssClasses("calendar-validation-error")
        .build();
  }

  /**
   * Creates a context optimized for dialog form validation. Uses individual notifications with
   * staggered timing for better UX.
   */
  public static ValidationErrorContext forDialogForms() {
    return ValidationErrorContext.builder()
        .position(Notification.Position.TOP_END)
        .duration(6000)
        .staggeredTiming(true)
        .staggerDelayMs(200) // Reduced delay for faster display
        .combineErrors(false) // Show individual notifications
        .maxIndividualErrors(10) // Allow more individual errors
        .additionalCssClasses("dialog-validation-error")
        .build();
  }

  /** Creates a context for general form validation in views. */
  public static ValidationErrorContext forGeneralForms() {
    return ValidationErrorContext.builder()
        .position(Notification.Position.BOTTOM_END)
        .duration(5000)
        .combineErrors(true)
        .additionalCssClasses("form-validation-error")
        .build();
  }

  /**
   * Creates a context optimized for showing individual validation errors without delay. Ideal for
   * cases where immediate feedback is needed.
   */
  public static ValidationErrorContext forIndividualErrorsImmediate() {
    return ValidationErrorContext.builder()
        .position(Notification.Position.TOP_END)
        .duration(5000)
        .staggeredTiming(false) // No delay for immediate feedback
        .combineErrors(false) // Individual notifications
        .maxIndividualErrors(15) // Show more errors if needed
        .additionalCssClasses("immediate-validation-error")
        .build();
  }

  /** Creates a default context with standard settings. */
  public static ValidationErrorContext defaultContext() {
    return ValidationErrorContext.builder().build();
  }
}
