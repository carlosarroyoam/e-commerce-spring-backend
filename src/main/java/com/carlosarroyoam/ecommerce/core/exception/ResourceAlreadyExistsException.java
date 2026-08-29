package com.carlosarroyoam.ecommerce.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza al intentar crear un recurso que ya existe (violación de una restricción de unicidad de
 * negocio). Se traduce a 409 Conflict.
 */
public class ResourceAlreadyExistsException extends ApplicationException {
  /**
   * Crea la excepción con el mensaje descriptivo indicado.
   *
   * @param message el mensaje descriptivo del fallo
   */
  public ResourceAlreadyExistsException(String message) {
    super(HttpStatus.CONFLICT, message);
  }
}
