package com.carlosarroyoam.ecommerce.core.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.lang.NonNull;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro que fuerza la resolución del token CSRF en cada petición, provocando que Spring Security
 * escriba la cookie {@code XSRF-TOKEN} usada por el esquema de doble envío con la cabecera
 * {@code X-XSRF-TOKEN}.
 */
@Component
public class CsrfCookieFilter extends OncePerRequestFilter {
  /**
   * Resuelve perezosamente el {@link CsrfToken} actual para forzar la escritura de su cookie
   * antes de continuar con la cadena de filtros.
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
    CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

    if (csrfToken != null) {
      csrfToken.getToken();
    }

    filterChain.doFilter(request, response);
  }
}
