package com.wornux.services.validation;

import com.wornux.services.implementations.ValidationErrorHandlerImpl;
import com.wornux.services.interfaces.ValidationErrorHandler;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ValidationErrorHandler implementation.
 * Tests the centralized validation error handling functionality.
 * 
 * @author Veterinary Management System
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class ValidationErrorHandlerTest {

  @Mock
  private ValidationMessageFormatter messageFormatter;
  
  @Mock
  private ValidationErrorNotificationService notificationService;
  
  @Mock
  private ConstraintViolation<?> violation1;
  
  @Mock
  private ConstraintViolation<?> violation2;
  
  private ValidationErrorHandler validationErrorHandler;

  @BeforeEach
  void setUp() {
    validationErrorHandler = new ValidationErrorHandlerImpl(messageFormatter, notificationService);
  }

  @Test
  void shouldHandleValidationErrorsWithDefaultContext() {
    // Given
    String originalMessage1 = "fecha de inicio debe ser en el futuro";
    String originalMessage2 = "mascota es requerida";
    String formattedMessage1 = "La fecha de inicio debe programarse para un momento futuro";
    String formattedMessage2 = "Debe seleccionar una mascota para la cita";

    when(violation1.getMessage()).thenReturn(originalMessage1);
    when(violation2.getMessage()).thenReturn(originalMessage2);
    when(messageFormatter.formatMessage(originalMessage1)).thenReturn(formattedMessage1);
    when(messageFormatter.formatMessage(originalMessage2)).thenReturn(formattedMessage2);

    Set<ConstraintViolation<?>> violations = Set.of(violation1, violation2);
    ConstraintViolationException exception = new ConstraintViolationException(violations);

    // When
    validationErrorHandler.handleValidationErrors(exception);

    // Then
    verify(messageFormatter).formatMessage(originalMessage1);
    verify(messageFormatter).formatMessage(originalMessage2);
    verify(notificationService).showNotification(anyString(), any(ValidationErrorContext.class));
  }

  @Test
  void shouldHandleValidationErrorsWithCustomContext() {
    // Given
    String originalMessage = "campo requerido";
    String formattedMessage = "Complete todos los campos obligatorios para continuar";
    
    when(violation1.getMessage()).thenReturn(originalMessage);
    when(messageFormatter.formatMessage(originalMessage)).thenReturn(formattedMessage);

    Set<ConstraintViolation<?>> violations = Set.of(violation1);
    ConstraintViolationException exception = new ConstraintViolationException(violations);
    
    ValidationErrorContext customContext = ValidationErrorContext.forCalendarOperations();

    // When
    validationErrorHandler.handleValidationErrors(exception, customContext);

    // Then
    verify(messageFormatter).formatMessage(originalMessage);
    verify(notificationService).showNotification(anyString(), eq(customContext));
  }

  @Test
  void shouldFormatValidationMessage() {
    // Given
    String originalMessage = "fecha de inicio debe ser en el futuro";
    String expectedFormatted = "La fecha de inicio debe programarse para un momento futuro";
    
    when(messageFormatter.formatMessage(originalMessage)).thenReturn(expectedFormatted);

    // When
    String result = validationErrorHandler.formatValidationMessage(originalMessage);

    // Then
    assertEquals(expectedFormatted, result);
    verify(messageFormatter).formatMessage(originalMessage);
  }

  @Test
  void shouldHandleEmptyViolationsGracefully() {
    // Given
    Set<ConstraintViolation<?>> emptyViolations = Set.of();
    ConstraintViolationException exception = new ConstraintViolationException(emptyViolations);

    // When/Then - should not throw exception
    assertDoesNotThrow(() -> validationErrorHandler.handleValidationErrors(exception));
    
    // Should not interact with notification service for empty violations
    verifyNoInteractions(notificationService);
  }

  @Test 
  void shouldCreateCalendarContextWithCorrectSettings() {
    // When
    ValidationErrorContext context = ValidationErrorContext.forCalendarOperations();
    
    // Then
    assertEquals(com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER, context.getPosition());
    assertEquals(8000, context.getDuration());
    assertTrue(context.isCombineErrors());
    assertTrue(context.getAdditionalCssClasses().contains("calendar"));
  }

  @Test
  void shouldCreateDialogContextWithCorrectSettings() {
    // When
    ValidationErrorContext context = ValidationErrorContext.forDialogForms();
    
    // Then
    assertEquals(com.vaadin.flow.component.notification.Notification.Position.TOP_END, context.getPosition());
    assertEquals(6000, context.getDuration());
    assertTrue(context.isStaggeredTiming());
    assertFalse(context.isCombineErrors());
    assertEquals(300, context.getStaggerDelayMs());
    assertTrue(context.getAdditionalCssClasses().contains("dialog"));
  }

  @Test
  void shouldCreateGeneralFormContextWithCorrectSettings() {
    // When
    ValidationErrorContext context = ValidationErrorContext.forGeneralForms();
    
    // Then
    assertEquals(com.vaadin.flow.component.notification.Notification.Position.BOTTOM_END, context.getPosition());
    assertEquals(5000, context.getDuration());
    assertTrue(context.isCombineErrors());
    assertTrue(context.getAdditionalCssClasses().contains("form"));
  }
}