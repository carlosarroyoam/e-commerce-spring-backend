package com.carlosarroyoam.ecommerce.core.filter;

import com.carlosarroyoam.ecommerce.auth.principal.AuthPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro que expone en el MDC la identidad del principal autenticado ({@link AuthPrincipal})
 * durante el procesamiento de la petición, bajo las claves {@code userId} y {@code principalType},
 * para que las líneas de log dejen constancia de quién originó la petición.
 *
 * <p>Se añade a la cadena de filtros de Spring Security justo después de la autenticación del JWT
 * (ver {@link com.carlosarroyoam.ecommerce.core.config.WebSecurityConfig}), momento en el que el
 * {@link SecurityContextHolder} ya está poblado. No se anota como componente para no registrarse
 * también como filtro de servlet de forma independiente.
 */
public class MdcUserContextFilter extends OncePerRequestFilter {
  public static final String USER_ID_KEY = "userId";
  public static final String PRINCIPAL_TYPE_KEY = "principalType";

  /**
   * Publica en el MDC los datos del principal autenticado, si lo hay, y los retira al completar la
   * cadena de filtros.
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
    boolean populated = populateMdc();

    try {
      filterChain.doFilter(request, response);
    } finally {
      if (populated) {
        MDC.remove(USER_ID_KEY);
        MDC.remove(PRINCIPAL_TYPE_KEY);
      }
    }
  }

  /**
   * Copia al MDC el identificador y el tipo del principal autenticado si el contexto de seguridad
   * contiene un {@link AuthPrincipal}.
   *
   * @return {@code true} si se publicaron valores en el MDC, {@code false} en caso contrario
   */
  private boolean populateMdc() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
      return false;
    }

    MDC.put(USER_ID_KEY, String.valueOf(principal.getId()));
    MDC.put(PRINCIPAL_TYPE_KEY, principal.getPrincipalType().name());
    return true;
  }
}
