package com.carlosarroyoam.ecommerce.core.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro que registra en el log cada petición HTTP procesada: método, URI, código de estado y
 * duración en milisegundos.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

  /**
   * Ejecuta la petición y registra su resultado una vez completada.
   *
   * @param request la petición HTTP entrante
   * @param response la respuesta HTTP saliente
   * @param filterChain la cadena de filtros a continuar
   */
  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {
    long startTime = System.currentTimeMillis();
    filterChain.doFilter(request, response);
    long duration = System.currentTimeMillis() - startTime;

    log.info(
        "{} {} {} - {} ms",
        request.getMethod(),
        request.getRequestURI(),
        response.getStatus(),
        duration);
  }
}
