package com.carlosarroyoam.ecommerce.core.exception;

import org.springframework.http.HttpStatus;

/** Se lanza cuando no existe el recurso solicitado. Se traduce a 404 Not Found. */
public class ResourceNotFoundException extends ApplicationException {
  /**
   * Crea la excepción con la clave de mensaje y los argumentos indicados.
   *
   * @param key la clave del mensaje en {@code messages.properties}
   * @param args los argumentos que reemplazan los placeholders del mensaje
   */
  public ResourceNotFoundException(String key, Object... args) {
    super(HttpStatus.NOT_FOUND, key, args);
  }
}
