package com.carlosarroyoam.ecommerce.core.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

/**
 * Construye instancias de {@link ProblemDetail} (RFC 9457, {@code application/problem+json}) con el
 * formato uniforme usado por {@link GlobalExceptionHandler} para todas las respuestas de error de
 * la API.
 */
@Component
public class ProblemDetailFactory {
  /**
   * Construye un problema sin errores de campo adicionales.
   *
   * @param status el estado HTTP del problema
   * @param detail el mensaje descriptivo del problema, puede ser nulo
   * @param request la petición HTTP que originó el error
   * @return el {@link ProblemDetail} resultante
   */
  public ProblemDetail build(HttpStatusCode status, String detail, HttpServletRequest request) {
    return build(status, detail, request, null);
  }

  /**
   * Construye un problema con un mapa de errores de validación por campo expuesto como la propiedad
   * de extensión {@code errors}.
   *
   * @param status el estado HTTP del problema
   * @param detail el mensaje descriptivo del problema, puede ser nulo
   * @param request la petición HTTP que originó el error
   * @param errors los errores de validación por campo, puede ser nulo
   * @return el {@link ProblemDetail} resultante
   */
  public ProblemDetail build(
      HttpStatusCode status,
      String detail,
      HttpServletRequest request,
      Map<String, String> errors) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(status);
    problemDetail.setInstance(URI.create(resolvePath(request)));

    if (detail != null) {
      problemDetail.setDetail(detail);
    }

    if (errors != null) {
      problemDetail.setProperty("errors", errors);
    }

    return problemDetail;
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
