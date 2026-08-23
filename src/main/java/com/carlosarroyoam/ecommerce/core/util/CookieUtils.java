package com.carlosarroyoam.ecommerce.core.util;

import com.carlosarroyoam.ecommerce.core.property.CookieProps;
import java.time.Duration;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Utilidades para crear y eliminar cookies HTTP con los atributos de seguridad estándar de la
 * aplicación ({@code HttpOnly}, {@code SameSite=Strict}, y {@code Secure} según
 * {@link CookieProps}).
 */
@Component
public class CookieUtils {
  private final CookieProps cookieProps;

  public CookieUtils(CookieProps cookieProps) {
    this.cookieProps = cookieProps;
  }

  /**
   * Crea una cookie con el valor y tiempo de vida indicados.
   *
   * @param cookieName el nombre de la cookie
   * @param cookieValue el valor de la cookie
   * @param duration el tiempo de vida de la cookie
   * @return la {@link ResponseCookie} resultante
   */
  public ResponseCookie createCookie(String cookieName, String cookieValue, Duration duration) {
    return ResponseCookie.from(cookieName, cookieValue)
        .httpOnly(true)
        .secure(cookieProps.getSecure())
        .sameSite("Strict")
        .path("/")
        .maxAge(duration)
        .build();
  }

  /**
   * Crea una cookie vacía y expirada, usada para eliminar una cookie existente en el navegador.
   *
   * @param cookieName el nombre de la cookie a eliminar
   * @return la {@link ResponseCookie} resultante
   */
  public ResponseCookie deleteCookie(String cookieName) {
    return ResponseCookie.from(cookieName, Strings.EMPTY)
        .httpOnly(true)
        .secure(cookieProps.getSecure())
        .sameSite("Strict")
        .path("/")
        .maxAge(Duration.ofMillis(0))
        .build();
  }
}
