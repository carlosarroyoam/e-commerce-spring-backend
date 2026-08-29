package com.carlosarroyoam.ecommerce.core.security;

import com.carlosarroyoam.ecommerce.core.exception.ProblemDetailFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Traduce un {@link AccessDeniedException} de Spring Security al mismo formato de error {@link
 * ProblemDetail} (RFC 9457) que usa el resto de la API, en vez de la respuesta por defecto de Spring
 * Security.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
  private final ProblemDetailFactory problemDetailFactory;
  private final ObjectMapper mapper;

  public CustomAccessDeniedHandler(ProblemDetailFactory problemDetailFactory, ObjectMapper mapper) {
    this.problemDetailFactory = problemDetailFactory;
    this.mapper = mapper;
  }

  /**
   * Escribe la respuesta 403 Forbidden con el cuerpo de error uniforme.
   *
   * @param request la petición HTTP que originó el acceso denegado
   * @param response la respuesta HTTP donde se escribe el error
   * @param ex la excepción de acceso denegado capturada
   */
  @Override
  public void handle(
      HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
      throws IOException {
    HttpStatus status = HttpStatus.FORBIDDEN;
    ProblemDetail problemDetail = problemDetailFactory.build(status, ex.getMessage(), request);

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    mapper.writeValue(response.getOutputStream(), problemDetail);
  }
}
