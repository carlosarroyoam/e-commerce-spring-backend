package com.carlosarroyoam.ecommerce.core.security;

import com.carlosarroyoam.ecommerce.core.exception.ExceptionLogger;
import com.carlosarroyoam.ecommerce.core.exception.ProblemDetailFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Traduce un fallo de autenticación de Spring Security al mismo formato de error {@link
 * ProblemDetail} (RFC 9457) que usa el resto de la API, en vez de la respuesta por defecto de
 * Spring Security.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final ProblemDetailFactory problemDetailFactory;
  private final ExceptionLogger exceptionLogger;
  private final ObjectMapper mapper;

  public CustomAuthenticationEntryPoint(
      ProblemDetailFactory problemDetailFactory,
      ExceptionLogger exceptionLogger,
      ObjectMapper mapper) {
    this.problemDetailFactory = problemDetailFactory;
    this.exceptionLogger = exceptionLogger;
    this.mapper = mapper;
  }

  /**
   * Escribe la respuesta 401 Unauthorized con el cuerpo de error uniforme.
   *
   * @param request la petición HTTP que originó el fallo de autenticación
   * @param response la respuesta HTTP donde se escribe el error
   * @param ex la excepción de autenticación capturada
   */
  @Override
  public void commence(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
      throws IOException {
    HttpStatus status = HttpStatus.UNAUTHORIZED;
    ProblemDetail problemDetail = problemDetailFactory.build(status, ex.getMessage(), request);
    exceptionLogger.log(status, ex.getMessage(), request, ex);

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    mapper.writeValue(response.getOutputStream(), problemDetail);
  }
}
