package com.carlosarroyoam.ecommerce.core.security;

import com.carlosarroyoam.ecommerce.core.exception.ApiExceptionResponseFactory;
import com.carlosarroyoam.ecommerce.core.exception.dto.AppExceptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Traduce un fallo de autenticación de Spring Security al mismo formato de error JSON
 * ({@link AppExceptionResponse}) que usa el resto de la API, en vez de la respuesta por defecto de
 * Spring Security.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final ApiExceptionResponseFactory apiExceptionResponseFactory;
  private final ObjectMapper mapper;

  public CustomAuthenticationEntryPoint(
      ApiExceptionResponseFactory apiExceptionResponseFactory, ObjectMapper mapper) {
    this.apiExceptionResponseFactory = apiExceptionResponseFactory;
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
    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, ex.getMessage(), request);

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    mapper.writeValue(response.getOutputStream(), appExceptionResponse);
  }
}
