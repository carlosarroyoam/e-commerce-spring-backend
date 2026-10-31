package com.carlosarroyoam.ecommerce.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base de las excepciones de dominio/aplicación. Cada subclase fija el código de estado HTTP con el
 * que {@link GlobalExceptionHandler} traduce el error a una respuesta {@link
 * org.springframework.http.ProblemDetail} (RFC 9457). Transporta la clave del mensaje en {@code
 * messages.properties} y sus argumentos; {@link #getMessage()} devuelve la clave sin resolver.
 */
@Getter
public abstract class ApplicationException extends RuntimeException {
  private final HttpStatus status;
  private final String key;
  private final Object[] args;

  /**
   * Crea la excepción con el estado HTTP, la clave de mensaje y los argumentos indicados.
   *
   * @param status el estado HTTP con el que debe responderse
   * @param key la clave del mensaje en {@code messages.properties}
   * @param args los argumentos que reemplazan los placeholders del mensaje
   */
  protected ApplicationException(HttpStatus status, String key, Object... args) {
    super(key);
    this.status = status;
    this.key = key;
    this.args = args;
  }

  /**
   * Crea la excepción con el estado HTTP, la clave de mensaje, la causa y los argumentos indicados.
   *
   * @param status el estado HTTP con el que debe responderse
   * @param key la clave del mensaje en {@code messages.properties}
   * @param cause la causa del fallo
   * @param args los argumentos que reemplazan los placeholders del mensaje
   */
  protected ApplicationException(HttpStatus status, String key, Throwable cause, Object... args) {
    super(key, cause);
    this.status = status;
    this.key = key;
    this.args = args;
  }
}
