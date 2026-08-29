package com.carlosarroyoam.ecommerce.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Excepción base de la aplicación: representa un fallo de dominio que ya sabe con qué estado HTTP
 * debe traducirse. Cada subclase fija su propio {@link HttpStatus} y {@link GlobalExceptionHandler}
 * la convierte en un {@link org.springframework.http.ProblemDetail} uniforme leyendo {@link
 * #getStatus()} y {@link #getMessage()}.
 */
@Getter
public abstract class ApplicationException extends RuntimeException {
  private final HttpStatus status;

  /**
   * Crea la excepción con el estado HTTP y el mensaje descriptivo indicados.
   *
   * @param status el estado HTTP con el que debe responderse
   * @param message el mensaje descriptivo del fallo
   */
  protected ApplicationException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }
}
