package com.carlosarroyoam.ecommerce.core.property;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuración de cookies bajo el prefijo {@code app.cookie}: si deben
 * marcarse como {@code Secure}.
 */
@Component
@ConfigurationProperties(prefix = "app.cookie")
@Getter
@Setter
public class CookieProps {
  @NotNull(message = "app.cookie.secure must not be null")
  private Boolean secure;
}
