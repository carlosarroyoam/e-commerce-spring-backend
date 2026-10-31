package com.carlosarroyoam.ecommerce.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando el estado actual del recurso impide la operación (conflictos de concurrencia o de
 * transición de estado). Se traduce a 409 Conflict.
 */
public class ConflictException extends ApplicationException {
  /**
   * Crea la excepción con la clave de mensaje y los argumentos indicados.
   *
   * @param key la clave del mensaje en {@code messages.properties}
   * @param args los argumentos que reemplazan los placeholders del mensaje
   */
  public ConflictException(String key, Object... args) {
    super(HttpStatus.CONFLICT, key, args);
  }
}
