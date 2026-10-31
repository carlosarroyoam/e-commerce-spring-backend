package com.carlosarroyoam.ecommerce.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza al intentar crear un recurso que ya existe (violación de una restricción de unicidad de
 * negocio). Se traduce a 409 Conflict.
 */
public class ResourceAlreadyExistsException extends ApplicationException {
  /**
   * Crea la excepción con la clave de mensaje y los argumentos indicados.
   *
   * @param key la clave del mensaje en {@code messages.properties}
   * @param args los argumentos que reemplazan los placeholders del mensaje
   */
  public ResourceAlreadyExistsException(String key, Object... args) {
    super(HttpStatus.CONFLICT, key, args);
  }
}
