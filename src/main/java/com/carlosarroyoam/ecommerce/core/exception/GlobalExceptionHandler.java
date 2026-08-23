package com.carlosarroyoam.ecommerce.core.exception;

import com.carlosarroyoam.ecommerce.core.exception.dto.AppExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 * aplicación (validación, autenticación/autorización, 404, método no soportado, genéricas) y las
 * convierte en un {@link AppExceptionResponse} uniforme mediante {@link
 * ApiExceptionResponseFactory}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private final ApiExceptionResponseFactory apiExceptionResponseFactory;

  public GlobalExceptionHandler(ApiExceptionResponseFactory apiExceptionResponseFactory) {
    this.apiExceptionResponseFactory = apiExceptionResponseFactory;
  }

  /**
   * Traduce una {@link ResponseStatusException} lanzada por los servicios al estado HTTP que ella
   * misma indica.
   */
  @ExceptionHandler({ResponseStatusException.class})
  public ResponseEntity<AppExceptionResponse> handleResponseStatus(
      ResponseStatusException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, ex.getReason(), request);

    return ResponseEntity.status(status).body(appExceptionResponse);
  }

  /** Mapea un cuerpo de petición ilegible o malformado a 400 Bad Request. */
  @ExceptionHandler({HttpMessageNotReadableException.class})
  public ResponseEntity<AppExceptionResponse> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, ex.getMessage(), request);

    return ResponseEntity.status(status).body(appExceptionResponse);
  }

  /** Mapea un parámetro con tipo incompatible a 400 Bad Request. */
  @ExceptionHandler({MethodArgumentTypeMismatchException.class})
  public ResponseEntity<AppExceptionResponse> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, ex.getMessage(), request);

    return ResponseEntity.status(status).body(appExceptionResponse);
  }

  /** Mapea una ruta sin handler registrado a 404 Not Found. */
  @ExceptionHandler({NoHandlerFoundException.class})
  public ResponseEntity<AppExceptionResponse> handleNoHandlerFound(
      NoHandlerFoundException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, ex.getMessage(), request);

    return ResponseEntity.status(status).body(appExceptionResponse);
  }

  /** Mapea un recurso estático no encontrado a 404 Not Found. */
  @ExceptionHandler({NoResourceFoundException.class})
  public ResponseEntity<AppExceptionResponse> handleNoResourceFound(
      NoResourceFoundException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, ex.getMessage(), request);

    return ResponseEntity.status(status).body(appExceptionResponse);
  }

  /** Mapea un método HTTP no soportado por el endpoint a 405 Method Not Allowed. */
  @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
  public ResponseEntity<AppExceptionResponse> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, ex.getMessage(), request);

    return ResponseEntity.status(status).body(appExceptionResponse);
  }

  /** Mapea un fallo de autenticación a 401 Unauthorized. */
  @ExceptionHandler({AuthenticationException.class})
  public ResponseEntity<AppExceptionResponse> handleAuthenticationException(
      AuthenticationException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.UNAUTHORIZED;
    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, ex.getMessage(), request);

    return ResponseEntity.status(status).body(appExceptionResponse);
  }

  /** Mapea un fallo de autorización a 403 Forbidden. */
  @ExceptionHandler({AccessDeniedException.class})
  public ResponseEntity<AppExceptionResponse> handleAccessDeniedException(
      AccessDeniedException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.FORBIDDEN;
    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, ex.getMessage(), request);

    return ResponseEntity.status(status).body(appExceptionResponse);
  }

  /**
   * Mapea errores de validación de Bean Validation a 422 Unprocessable Entity, incluyendo el
   * detalle de cada campo inválido.
   */
  @ExceptionHandler({MethodArgumentNotValidException.class})
  public ResponseEntity<AppExceptionResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
    Map<String, String> details =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    FieldError::getField,
                    FieldError::getDefaultMessage,
                    (existing, replacement) -> existing));

    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, "Invalid request data", request, details);

    return ResponseEntity.status(status).body(appExceptionResponse);
  }

  /**
   * Último manejador de respaldo: registra la excepción y responde 500 Internal Server Error sin
   * exponer detalles internos en el cuerpo de la respuesta.
   */
  @ExceptionHandler({Exception.class})
  public ResponseEntity<AppExceptionResponse> handleException(
      Exception ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, "Whoops! Something went wrong", request);

    log.error("Whoops! Something went wrong: ", ex);

    return ResponseEntity.status(status).body(appExceptionResponse);
  }
}
