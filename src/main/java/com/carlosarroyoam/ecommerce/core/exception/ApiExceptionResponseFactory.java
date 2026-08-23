package com.carlosarroyoam.ecommerce.core.exception;

import com.carlosarroyoam.ecommerce.core.exception.dto.AppExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Construye instancias de {@link AppExceptionResponse} con el formato uniforme usado por
 * {@link GlobalExceptionHandler} para todas las respuestas de error de la API.
 */
@Component
public class ApiExceptionResponseFactory {
  /**
   * Construye una respuesta de error sin detalles adicionales.
   *
   * @param status el estado HTTP de la respuesta
   * @param message el mensaje de error
   * @param request la petición HTTP que originó el error
   * @return la {@link AppExceptionResponse} resultante
   */
  public AppExceptionResponse build(HttpStatus status, String message, HttpServletRequest request) {
    return build(status, message, request, null);
  }

  /**
   * Construye una respuesta de error con detalles adicionales (p. ej. errores de validación por
   * campo).
   *
   * @param status el estado HTTP de la respuesta
   * @param message el mensaje de error
   * @param request la petición HTTP que originó el error
   * @param details detalles adicionales del error, puede ser nulo
   * @return la {@link AppExceptionResponse} resultante
   */
  public AppExceptionResponse build(
      HttpStatus status, String message, HttpServletRequest request, Map<String, String> details) {
    return AppExceptionResponse.builder()
        .message(message)
        .error(status.getReasonPhrase())
        .status(status.value())
        .path(resolvePath(request))
        .timestamp(ZonedDateTime.now(ZoneId.of("UTC")))
        .details(details)
        .build();
  }

  /**
   * Resuelve la ruta de la petición que originó el error, prefiriendo el atributo de error del
   * servlet cuando la excepción se maneja a través del reenvío de error de Spring.
   *
   * @param request la petición HTTP
   * @return la ruta resuelta
   */
  private String resolvePath(HttpServletRequest request) {
    Object uri = request.getAttribute("jakarta.servlet.error.request_uri");
    return uri != null ? uri.toString() : request.getRequestURI();
  }
}
