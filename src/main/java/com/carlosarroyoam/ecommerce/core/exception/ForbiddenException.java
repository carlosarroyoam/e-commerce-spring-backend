package com.carlosarroyoam.ecommerce.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando el usuario está autenticado pero no tiene permisos para la operación solicitada.
 * Se traduce a 403 Forbidden.
 */
public class ForbiddenException extends ApplicationException {
  /**
   * Crea la excepción con la clave de mensaje y los argumentos indicados.
   *
   * @param key la clave del mensaje en {@code messages.properties}
   * @param args los argumentos que reemplazan los placeholders del mensaje
   */
  public ForbiddenException(String key, Object... args) {
    super(HttpStatus.FORBIDDEN, key, args);
  }
}
