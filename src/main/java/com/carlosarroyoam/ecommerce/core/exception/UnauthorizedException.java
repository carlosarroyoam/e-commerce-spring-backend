package com.carlosarroyoam.ecommerce.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando el usuario no está autenticado o su credencial (por ejemplo, el refresh token) no
 * es válida. Se traduce a 401 Unauthorized.
 */
public class UnauthorizedException extends ApplicationException {
  /**
   * Crea la excepción con la clave de mensaje y los argumentos indicados.
   *
   * @param key la clave del mensaje en {@code messages.properties}
   * @param args los argumentos que reemplazan los placeholders del mensaje
   */
  public UnauthorizedException(String key, Object... args) {
    super(HttpStatus.UNAUTHORIZED, key, args);
  }
}
