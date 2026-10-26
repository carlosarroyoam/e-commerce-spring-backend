package com.carlosarroyoam.ecommerce.support.testutils;

import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * Construye un {@link MessageSource} que lee {@code messages.properties}, para usar en {@code
 * MockMvc.standaloneSetup(...)} y tests unitarios donde no hay contexto de Spring Boot que lo
 * autoconfigure.
 */
public final class TestMessageSources {
  private TestMessageSources() {
    throw new IllegalAccessError("Illegal access to utility class");
  }

  /**
   * Crea un {@link MessageSource} con basename {@code messages} y codificación UTF-8.
   *
   * @return el {@link MessageSource} configurado
   */
  public static MessageSource create() {
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasename("messages");
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setFallbackToSystemLocale(false);
    return messageSource;
  }
}
