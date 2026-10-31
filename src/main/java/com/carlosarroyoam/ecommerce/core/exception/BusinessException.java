package com.carlosarroyoam.ecommerce.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando una operación viola una regla de negocio. Se traduce a 422 Unprocessable Entity,
 * el mismo estado que usan los fallos de validación de Bean Validation.
 */
public class BusinessException extends ApplicationException {
  /**
   * Crea la excepción con la clave de mensaje y los argumentos indicados.
   *
   * @param key la clave del mensaje en {@code messages.properties}
   * @param args los argumentos que reemplazan los placeholders del mensaje
   */
  public BusinessException(String key, Object... args) {
    super(HttpStatus.UNPROCESSABLE_ENTITY, key, args);
  }
}
