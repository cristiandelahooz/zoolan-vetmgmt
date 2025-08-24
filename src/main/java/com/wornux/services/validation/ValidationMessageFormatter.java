package com.wornux.services.validation;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Formats validation messages to be user-friendly and professional.
 * Transforms technical validation messages into clear, actionable feedback in Spanish.
 * 
 * <p>This formatter uses pattern matching to identify common validation scenarios
 * and provides contextual, professional messaging that guides users toward
 * resolving validation issues.</p>
 * 
 * @author Veterinary Management System
 * @since 1.0.0
 */
@Component
public class ValidationMessageFormatter {

  private final Map<Pattern, String> messagePatterns;

  public ValidationMessageFormatter() {
    this.messagePatterns = initializeMessagePatterns();
  }

  /**
   * Formats a validation message to be user-friendly and professional.
   * 
   * @param originalMessage the original validation message from constraints
   * @return formatted user-friendly message in Spanish
   */
  public String formatMessage(String originalMessage) {
    if (originalMessage == null || originalMessage.trim().isEmpty()) {
      return "Error de validación desconocido.";
    }

    String message = originalMessage.trim();

    // Check against known patterns for professional formatting
    for (Map.Entry<Pattern, String> entry : messagePatterns.entrySet()) {
      if (entry.getKey().matcher(message).find()) {
        return entry.getValue();
      }
    }

    // Apply general formatting rules if no specific pattern matches
    return applyGeneralFormatting(message);
  }

  /**
   * Initializes the mapping between message patterns and their professional equivalents.
   */
  private Map<Pattern, String> initializeMessagePatterns() {
    Map<Pattern, String> patterns = new HashMap<>();

    // Appointment-specific patterns
    patterns.put(
        Pattern.compile("fecha de inicio.*futuro", Pattern.CASE_INSENSITIVE),
        "La fecha de inicio debe programarse para un momento futuro"
    );

    patterns.put(
        Pattern.compile("fecha de fin.*futuro|fecha de cierre.*futuro", Pattern.CASE_INSENSITIVE),
        "La fecha de finalización debe programarse para un momento futuro"
    );

    patterns.put(
        Pattern.compile("fecha de inicio.*anterior.*fecha de fin", Pattern.CASE_INSENSITIVE),
        "La fecha de inicio debe ser anterior a la fecha de finalización"
    );

    patterns.put(
        Pattern.compile("duración.*mínimo|duración.*corta", Pattern.CASE_INSENSITIVE),
        "La duración de la cita debe ser de al menos 15 minutos"
    );

    patterns.put(
        Pattern.compile("conflicto.*horario|solapamiento.*cita", Pattern.CASE_INSENSITIVE),
        "Ya existe una cita programada en este horario. Seleccione un horario diferente"
    );

    // Client and Pet patterns
    patterns.put(
        Pattern.compile("información.*cliente.*registrado", Pattern.CASE_INSENSITIVE),
        "Seleccione un cliente registrado o proporcione datos del cliente invitado"
    );

    patterns.put(
        Pattern.compile("mascota.*cliente.*registrado", Pattern.CASE_INSENSITIVE),
        "Para seleccionar una mascota, debe asociarla con un cliente registrado"
    );

    patterns.put(
        Pattern.compile("mascota.*requerida|debe.*seleccionar.*mascota", Pattern.CASE_INSENSITIVE),
        "Debe seleccionar una mascota para la cita"
    );

    // General field validation patterns
    patterns.put(
        Pattern.compile("campo.*requerido|no puede estar vacío|obligatorio", Pattern.CASE_INSENSITIVE),
        "Complete todos los campos obligatorios para continuar"
    );

    patterns.put(
        Pattern.compile("formato.*correo|email.*inválido", Pattern.CASE_INSENSITIVE),
        "Ingrese una dirección de correo electrónico válida"
    );

    patterns.put(
        Pattern.compile("formato.*teléfono|teléfono.*inválido", Pattern.CASE_INSENSITIVE),
        "Ingrese un número de teléfono válido (formato: 809XXXXXXX)"
    );

    patterns.put(
        Pattern.compile("longitud.*mínima|muy corto", Pattern.CASE_INSENSITIVE),
        "El texto ingresado es muy corto. Verifique la longitud mínima requerida"
    );

    patterns.put(
        Pattern.compile("longitud.*máxima|muy largo", Pattern.CASE_INSENSITIVE),
        "El texto ingresado es muy largo. Verifique la longitud máxima permitida"
    );

    // Business rule patterns
    patterns.put(
        Pattern.compile("horario.*negocio|fuera.*horario", Pattern.CASE_INSENSITIVE),
        "La cita debe programarse dentro del horario de atención (8:00 AM - 8:00 PM)"
    );

    patterns.put(
        Pattern.compile("día.*no.*laborable|día.*cerrado", Pattern.CASE_INSENSITIVE),
        "No se pueden programar citas en días no laborables"
    );

    patterns.put(
        Pattern.compile("veterinario.*disponible", Pattern.CASE_INSENSITIVE),
        "No hay veterinarios disponibles en el horario seleccionado"
    );

    // Data integrity patterns
    patterns.put(
        Pattern.compile("ya.*existe|duplicado", Pattern.CASE_INSENSITIVE),
        "Ya existe un registro con esta información. Verifique los datos ingresados"
    );

    patterns.put(
        Pattern.compile("no.*encontrado|no.*existe", Pattern.CASE_INSENSITIVE),
        "El registro solicitado no fue encontrado. Verifique la información"
    );

    return patterns;
  }

  /**
   * Applies general formatting rules when no specific pattern matches.
   */
  private String applyGeneralFormatting(String message) {
    String formatted = message.trim();

    // Ensure proper capitalization
    if (!formatted.isEmpty()) {
      formatted = Character.toUpperCase(formatted.charAt(0)) + formatted.substring(1);
    }

    // Ensure proper punctuation
    if (!formatted.endsWith(".") && !formatted.endsWith("!") && !formatted.endsWith("?")) {
      formatted += ".";
    }

    // Replace technical terms with user-friendly equivalents
    formatted = formatted
        .replaceAll("\\bConstraintViolation\\b", "Error de validación")
        .replaceAll("\\bValidation\\b", "Validación")
        .replaceAll("\\bmust\\b", "debe")
        .replaceAll("\\bcannot\\b", "no puede")
        .replaceAll("\\binvalid\\b", "inválido")
        .replaceAll("\\brequired\\b", "requerido")
        .replaceAll("\\bnull\\b", "vacío");

    return formatted;
  }
}