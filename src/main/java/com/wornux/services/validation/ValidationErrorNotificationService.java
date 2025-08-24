package com.wornux.services.validation;

import com.vaadin.flow.component.notification.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Service responsible for displaying validation error notifications to users. Provides consistent
 * styling and behavior for validation error messages across the application.
 *
 * <p>This service encapsulates the notification display logic, allowing for consistent error
 * presentation while supporting customization through context configuration.
 *
 * @author Veterinary Management System
 * @since 1.0.0
 */
@Slf4j
@Service
public class ValidationErrorNotificationService {

  /**
   * Shows a notification with the specified message and context configuration.
   *
   * @param message the message to display
   * @param context the context containing display configuration
   */
  public void showNotification(String message, ValidationErrorContext context) {
    if (!StringUtils.hasText(message)) {
      log.warn("Attempted to show notification with empty message");
      return;
    }

    try {
      Notification notification = createNotification(message, context);
      notification.open();

      log.debug("Displayed validation error notification: {}", message);
    } catch (Exception e) {
      log.error("Failed to display validation error notification", e);
      // Fallback to simple notification
      showFallbackNotification(message);
    }
  }

  /** Creates a notification with proper styling and configuration. */
  private Notification createNotification(String message, ValidationErrorContext context) {
    Notification notification = new Notification();

    // Basic configuration
    notification.setText(message);
    notification.setDuration(context.getDuration());
    notification.setPosition(context.getPosition());

    // Theme variants
    notification.addThemeVariants(context.getThemeVariant());

    // Additional styling
    if (StringUtils.hasText(context.getAdditionalCssClasses())) {
      String[] classes = context.getAdditionalCssClasses().split("\\s+");
      for (String cssClass : classes) {
        if (StringUtils.hasText(cssClass)) {
          notification.getElement().getThemeList().add(cssClass);
        }
      }
    }

    // Accessibility attributes
    notification.getElement().setAttribute("role", "alert");
    notification.getElement().setAttribute("aria-live", "polite");

    // Professional styling enhancements
    enhanceNotificationStyling(notification, context);

    return notification;
  }

  /** Enhances notification styling for professional appearance. */
  private void enhanceNotificationStyling(
      Notification notification, ValidationErrorContext context) {
    // Add consistent validation error styling
    notification.getElement().getThemeList().add("validation-error");

    // Enhanced visual styling through CSS custom properties
    notification
        .getElement()
        .getStyle()
        .set("--lumo-font-family", "var(--lumo-font-family)")
        .set("box-shadow", "var(--lumo-box-shadow-m)")
        .set("border-radius", "var(--lumo-border-radius-m)");

    // Context-specific enhancements
    if (context.getAdditionalCssClasses().contains("calendar")) {
      notification.getElement().getStyle().set("font-weight", "500").set("max-width", "400px");
    } else if (context.getAdditionalCssClasses().contains("dialog")) {
      notification
          .getElement()
          .getStyle()
          .set("font-size", "var(--lumo-font-size-s)")
          .set("max-width", "350px");
    }
  }

  /** Shows a basic fallback notification when the enhanced notification fails. */
  private void showFallbackNotification(String message) {
    try {
      Notification fallback = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
      fallback.addThemeVariants(
          com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR);

      log.debug("Displayed fallback validation notification: {}", message);
    } catch (Exception e) {
      log.error("Failed to display even fallback notification", e);
    }
  }
}
