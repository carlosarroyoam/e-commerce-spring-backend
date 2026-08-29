package com.carlosarroyoam.ecommerce.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando el estado actual del recurso impide la operación (conflictos de concurrencia o de
 * transición de estado). Se traduce a 409 Conflict.
 */
public class ConflictException extends ApplicationException {
  /**
   * Crea la excepción con el mensaje descriptivo indicado.
   *
   * @param message el mensaje descriptivo del fallo
   */
  public ConflictException(String message) {
    super(HttpStatus.CONFLICT, message);
  }
}
