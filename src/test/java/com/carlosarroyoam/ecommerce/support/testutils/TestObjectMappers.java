package com.carlosarroyoam.ecommerce.support.testutils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Construye un {@link ObjectMapper} que replica {@code spring.jackson.property-naming-strategy}
 * (SNAKE_CASE) para usar en {@code MockMvc.standaloneSetup(...)}, donde no hay contexto de Spring
 * Boot que aplique esa configuracion automaticamente al {@code ObjectMapper} por defecto.
 */
public final class TestObjectMappers {
  private TestObjectMappers() {
    throw new IllegalAccessError("Illegal access to utility class");
  }

  /**
   * Crea un {@link ObjectMapper} en snake_case via {@link Jackson2ObjectMapperBuilder}, que es lo
   * que usa Spring Boot internamente: descubre via SPI los mismos modulos que registra
   * automaticamente (soporte de {@code java.time.*} y {@code jackson-module-parameter-names},
   * necesario para deserializar DTOs con Lombok {@code @Builder}) y, ademas, registra el {@code
   * ProblemDetailJacksonMixin} que serializa {@link org.springframework.http.ProblemDetail} en la
   * forma plana de RFC 9457 (ese mixin no se descubre via {@code findAndRegisterModules()}).
   *
   * @return el {@link ObjectMapper} configurado
   */
  public static ObjectMapper snakeCase() {
    return Jackson2ObjectMapperBuilder.json()
        .findModulesViaServiceLoader(true)
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build();
  }
}
