package com.carlosarroyoam.ecommerce.core.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Punto único de traducción de excepciones a respuestas HTTP: captura cada tipo de excepción de la
 * aplicación (las {@link ApplicationException} de dominio, validación, autenticación/autorización,
 * 404, método no soportado, genéricas) y las convierte en un {@link ProblemDetail} uniforme (RFC
 * 9457) mediante {@link ProblemDetailFactory}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private final ProblemDetailFactory problemDetailFactory;

  public GlobalExceptionHandler(ProblemDetailFactory problemDetailFactory) {
    this.problemDetailFactory = problemDetailFactory;
  }

  /**
   * Traduce una {@link ApplicationException} de dominio al estado HTTP que ella misma declara.
   */
  @ExceptionHandler({ApplicationException.class})
  public ProblemDetail handleApplicationException(
      ApplicationException ex, HttpServletRequest request) {
    return problemDetailFactory.build(ex.getStatus(), ex.getMessage(), request);
  }

  /**
   * Traduce una {@link ResponseStatusException} al estado HTTP que ella misma indica. Se conserva
   * como red de seguridad para los casos que todavía la lanzan (p. ej. la funcionalidad de
   * recuperación de contraseña aún no implementada).
   */
  @ExceptionHandler({ResponseStatusException.class})
  public ProblemDetail handleResponseStatus(
      ResponseStatusException ex, HttpServletRequest request) {
    return problemDetailFactory.build(ex.getStatusCode(), ex.getReason(), request);
  }

  /** Mapea un cuerpo de petición ilegible o malformado a 400 Bad Request. */
  @ExceptionHandler({HttpMessageNotReadableException.class})
  public ProblemDetail handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    return problemDetailFactory.build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
  }

  /** Mapea un parámetro con tipo incompatible a 400 Bad Request. */
  @ExceptionHandler({MethodArgumentTypeMismatchException.class})
  public ProblemDetail handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    return problemDetailFactory.build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
  }

  /** Mapea una ruta sin handler registrado a 404 Not Found. */
  @ExceptionHandler({NoHandlerFoundException.class})
  public ProblemDetail handleNoHandlerFound(
      NoHandlerFoundException ex, HttpServletRequest request) {
    return problemDetailFactory.build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  /** Mapea un recurso estático no encontrado a 404 Not Found. */
  @ExceptionHandler({NoResourceFoundException.class})
  public ProblemDetail handleNoResourceFound(
      NoResourceFoundException ex, HttpServletRequest request) {
    return problemDetailFactory.build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  /** Mapea un método HTTP no soportado por el endpoint a 405 Method Not Allowed. */
  @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
  public ProblemDetail handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
    return problemDetailFactory.build(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request);
  }

  /** Mapea un fallo de autenticación a 401 Unauthorized. */
  @ExceptionHandler({AuthenticationException.class})
  public ProblemDetail handleAuthenticationException(
      AuthenticationException ex, HttpServletRequest request) {
    return problemDetailFactory.build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
  }

  /** Mapea un fallo de autorización a 403 Forbidden. */
  @ExceptionHandler({AccessDeniedException.class})
  public ProblemDetail handleAccessDeniedException(
      AccessDeniedException ex, HttpServletRequest request) {
    return problemDetailFactory.build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
  }

  /**
   * Mapea errores de validación de Bean Validation a 422 Unprocessable Entity, incluyendo el
   * detalle de cada campo inválido en la propiedad de extensión {@code errors}.
   */
  @ExceptionHandler({MethodArgumentNotValidException.class})
  public ProblemDetail handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, String> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    FieldError::getField,
                    FieldError::getDefaultMessage,
                    (existing, replacement) -> existing));

    return problemDetailFactory.build(
        HttpStatus.UNPROCESSABLE_ENTITY, "Invalid request data", request, errors);
  }

  /**
   * Último manejador de respaldo: registra la excepción y responde 500 Internal Server Error sin
   * exponer detalles internos en el cuerpo de la respuesta.
   */
  @ExceptionHandler({Exception.class})
  public ProblemDetail handleException(Exception ex, HttpServletRequest request) {
    log.error("Whoops! Something went wrong: ", ex);

    return problemDetailFactory.build(
        HttpStatus.INTERNAL_SERVER_ERROR, "Whoops! Something went wrong", request);
  }
}
