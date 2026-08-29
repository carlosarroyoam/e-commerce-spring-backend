package com.carlosarroyoam.ecommerce.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza ante validaciones de negocio que {@code @Valid} no puede cubrir (por ejemplo, reglas que
 * dependen de varios campos o del estado persistido). Se traduce a 422 Unprocessable Entity.
 */
public class ValidationException extends ApplicationException {
  /**
   * Crea la excepción con el mensaje descriptivo indicado.
   *
   * @param message el mensaje descriptivo del fallo
   */
  public ValidationException(String message) {
    super(HttpStatus.UNPROCESSABLE_ENTITY, message);
  }
}
