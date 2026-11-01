package com.carlosarroyoam.ecommerce.support.security;

import com.carlosarroyoam.ecommerce.core.constant.CommonConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * {@link RequestPostProcessor} para sobrescribir la identidad que un {@code MockMvc} de integración
 * define por defecto en {@code defaultRequest}: permite enviar la petición como otro principal o
 * sin autenticar.
 */
public final class AuthRequestPostProcessors {
  private AuthRequestPostProcessors() {
    throw new IllegalAccessError(CommonConstants.ILLEGAL_ACCESS_ERROR);
  }

  /**
   * Reemplaza la cabecera {@code Authorization} por un {@code Bearer} con el token indicado.
   *
   * @param token el JWT compacto, p. ej. el devuelto por {@link JwtTestTokenFactory}
   * @return el post-procesador de la petición
   */
  public static RequestPostProcessor bearer(String token) {
    return request -> {
      request.removeHeader(HttpHeaders.AUTHORIZATION);
      request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
      return request;
    };
  }

  /**
   * Elimina la cabecera {@code Authorization}, de modo que la petición llega sin autenticar.
   *
   * @return el post-procesador de la petición
   */
  public static RequestPostProcessor withoutAuthentication() {
    return request -> {
      request.removeHeader(HttpHeaders.AUTHORIZATION);
      return request;
    };
  }
}
