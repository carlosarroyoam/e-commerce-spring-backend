package com.carlosarroyoam.ecommerce.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza ante validaciones de negocio que {@code @Valid} no puede cubrir (por ejemplo, reglas que
 * dependen de varios campos o del estado persistido). Se traduce a 422 Unprocessable Entity.
 */
public class ValidationException extends ApplicationException {
  /**
   * Crea la excepción con la clave de mensaje y los argumentos indicados.
   *
   * @param key la clave del mensaje en {@code messages.properties}
   * @param args los argumentos que reemplazan los placeholders del mensaje
   */
  public ValidationException(String key, Object... args) {
    super(HttpStatus.UNPROCESSABLE_ENTITY, key, args);
  }
}
