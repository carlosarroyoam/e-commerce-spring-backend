package com.carlosarroyoam.ecommerce.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando una operación viola una regla de negocio. Se traduce a 422 Unprocessable Entity,
 * el mismo estado que usan los fallos de validación de Bean Validation.
 */
public class BusinessException extends ApplicationException {
  /**
   * Crea la excepción con el mensaje descriptivo indicado.
   *
   * @param message el mensaje descriptivo del fallo
   */
  public BusinessException(String message) {
    super(HttpStatus.UNPROCESSABLE_ENTITY, message);
  }
}
