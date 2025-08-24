package com.wornux.services.implementations;

import com.vaadin.flow.component.UI;
import com.wornux.services.interfaces.ValidationErrorHandler;
import com.wornux.services.validation.ValidationErrorContext;
import com.wornux.services.validation.ValidationErrorNotificationService;
import com.wornux.services.validation.ValidationMessageFormatter;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementation of ValidationErrorHandler that provides centralized validation error handling with
 * context-aware customization for different UI scenarios.
 *
 * <p>This service processes constraint violations and displays user-friendly notifications
 * according to the provided context configuration. It supports both combined and individual error
 * display strategies.
 *
 * @author Veterinary Management System
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationErrorHandlerImpl implements ValidationErrorHandler {

  private final ValidationMessageFormatter messageFormatter;
  private final ValidationErrorNotificationService notificationService;

  @Override
  public void handleValidationErrors(
      ConstraintViolationException exception, ValidationErrorContext context) {
    log.debug("Handling validation errors with context: {}", context);

    List<String> formattedMessages =
        exception.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .map(this::formatValidationMessage)
            .toList();

    if (formattedMessages.isEmpty()) {
      log.warn("No validation messages found in constraint violation exception");
      return;
    }

    if (context.isCombineErrors()) {
      showCombinedErrors(formattedMessages, context);
    } else {
      showIndividualErrors(formattedMessages, context);
    }
  }

  @Override
  public void handleValidationErrors(ConstraintViolationException exception) {
    handleValidationErrors(exception, ValidationErrorContext.defaultContext());
  }

  @Override
  public String formatValidationMessage(String originalMessage) {
    return messageFormatter.formatMessage(originalMessage);
  }

  /** Shows multiple validation errors combined into a single notification. */
  private void showCombinedErrors(List<String> messages, ValidationErrorContext context) {
    StringBuilder combinedMessage = new StringBuilder("Errores de validación:");

    for (String message : messages) {
      combinedMessage.append("\n• ").append(message);
    }

    notificationService.showNotification(combinedMessage.toString(), context);
  }

  /**
   * Shows validation errors as separate notifications with reliable server-side sequencing. This
   * approach eliminates JavaScript dependencies and ensures all errors are displayed.
   */
  private void showIndividualErrors(List<String> messages, ValidationErrorContext context) {
    log.debug("Displaying {} individual validation errors", messages.size());

    if (context.isStaggeredTiming()) {
      showServerSideSequentialNotifications(messages, context);
    } else {
      // Show all notifications immediately without truncation
      messages.forEach(message -> notificationService.showNotification(message, context));
    }
  }

  /**
   * Shows notifications with minimal delays to allow Vaadin's natural stacking. This method shows
   * all notifications quickly without interfering with each other.
   */
  private void showServerSideSequentialNotifications(
      List<String> messages, ValidationErrorContext context) {
    log.debug("Starting sequential notification display for {} messages", messages.size());

    UI currentUI = UI.getCurrent();
    if (currentUI == null) {
      log.warn("No UI context available, showing notifications immediately");
      // Fallback to immediate notifications
      messages.forEach(message -> notificationService.showNotification(message, context));
      return;
    }

    // Show all notifications with minimal delays to allow natural stacking
    for (int i = 0; i < messages.size(); i++) {
      final String message = messages.get(i);

      if (i > 0) {
        try {
          Thread.sleep(50L * i); // Simple 50ms delay between notifications
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          log.warn("Notification delay interrupted", e);
          break;
        }
      }

      try {
        notificationService.showNotification(message, context);
        log.debug("Displayed notification {} of {}: {}", i + 1, messages.size(), message);
      } catch (Exception e) {
        log.error("Failed to display notification: {}", message, e);
      }
    }
  }
}
