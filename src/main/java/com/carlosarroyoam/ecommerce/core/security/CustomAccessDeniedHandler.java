package com.carlosarroyoam.ecommerce.core.security;

import com.carlosarroyoam.ecommerce.core.exception.ApiExceptionResponseFactory;
import com.carlosarroyoam.ecommerce.core.exception.dto.AppExceptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Traduce un {@link AccessDeniedException} de Spring Security al mismo formato de error JSON
 * ({@link AppExceptionResponse}) que usa el resto de la API, en vez de la respuesta por defecto de
 * Spring Security.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
  private final ApiExceptionResponseFactory apiExceptionResponseFactory;
  private final ObjectMapper mapper;

  public CustomAccessDeniedHandler(
      ApiExceptionResponseFactory apiExceptionResponseFactory, ObjectMapper mapper) {
    this.apiExceptionResponseFactory = apiExceptionResponseFactory;
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
    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, ex.getMessage(), request);

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    mapper.writeValue(response.getOutputStream(), appExceptionResponse);
  }
}
