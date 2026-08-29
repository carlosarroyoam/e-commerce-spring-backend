package com.carlosarroyoam.ecommerce.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando el usuario no está autenticado o su credencial (por ejemplo, el refresh token) no
 * es válida. Se traduce a 401 Unauthorized.
 */
public class UnauthorizedException extends ApplicationException {
  /**
   * Crea la excepción con el mensaje descriptivo indicado.
   *
   * @param message el mensaje descriptivo del fallo
   */
  public UnauthorizedException(String message) {
    super(HttpStatus.UNAUTHORIZED, message);
  }
}
