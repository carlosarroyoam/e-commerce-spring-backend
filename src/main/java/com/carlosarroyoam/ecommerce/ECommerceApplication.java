package com.carlosarroyoam.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Punto de entrada de la aplicación Spring Boot del API de e-commerce. */
@SpringBootApplication
public class ECommerceApplication {
  /**
   * Arranca el contexto de Spring Boot.
   *
   * @param args argumentos de línea de comandos
   */
  public static void main(String[] args) {
    SpringApplication.run(ECommerceApplication.class, args);
  }
}
