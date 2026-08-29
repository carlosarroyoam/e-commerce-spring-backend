package com.carlosarroyoam.ecommerce.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando el usuario está autenticado pero no tiene permisos para la operación solicitada.
 * Se traduce a 403 Forbidden.
 */
public class ForbiddenException extends ApplicationException {
  /**
   * Crea la excepción con el mensaje descriptivo indicado.
   *
   * @param message el mensaje descriptivo del fallo
   */
  public ForbiddenException(String message) {
    super(HttpStatus.FORBIDDEN, message);
  }
}
