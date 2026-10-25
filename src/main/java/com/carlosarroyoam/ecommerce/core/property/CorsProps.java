package com.carlosarroyoam.ecommerce.core.property;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuración CORS bajo el prefijo {@code app.cors}: orígenes, métodos y
 * cabeceras permitidos/expuestos, y si se permiten credenciales.
 */
@Component
@ConfigurationProperties(prefix = "app.cors")
@Getter
@Setter
public class CorsProps {
  @NotEmpty(message = "app.cors.allowed-origins must not be empty")
  private List<String> allowedOrigins;

  @NotEmpty(message = "app.cors.allowed-methods must not be empty")
  private List<String> allowedMethods;

  @NotEmpty(message = "app.cors.allowed-headers must not be empty")
  private List<String> allowedHeaders;

  @NotEmpty(message = "app.cors.exposed-headers must not be empty")
  private List<String> exposedHeaders;

  @NotNull(message = "app.cors.allow-credentials must not be null")
  private Boolean allowCredentials;
}
