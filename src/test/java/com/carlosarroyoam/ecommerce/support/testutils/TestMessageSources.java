package com.carlosarroyoam.ecommerce.support.testutils;

import com.carlosarroyoam.ecommerce.core.constant.CommonConstants;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * Construye un {@link MessageSource} que lee {@code messages.properties}, para usar en {@code
 * MockMvc.standaloneSetup(...)} y tests unitarios donde no hay contexto de Spring Boot que lo
 * autoconfigure.
 */
public final class TestMessageSources {
  private TestMessageSources() {
    throw new IllegalAccessError(CommonConstants.ILLEGAL_ACCESS_ERROR);
  }

  /**
   * Crea un {@link MessageSource} con basename {@code messages} y codificación UTF-8.
   *
   * @return el {@link MessageSource} configurado
   */
  public static MessageSource messages() {
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasename("messages");
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setFallbackToSystemLocale(false);
    return messageSource;
  }
}
