package com.carlosarroyoam.ecommerce.core.property;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuración de JWT bajo el prefijo {@code application.jwt}: tiempos de vida del
 * access token y del refresh token, y el tiempo de vida máximo absoluto de un refresh token.
 */
@Component
@ConfigurationProperties(prefix = "application.jwt")
@Getter
@Setter
public class JwtProps {
  @NotNull(message = "access-token-ttl-ms must not be null")
  private long accessTokenTtlMs;

  @NotNull(message = "refresh-token-ttl-ms must not be null")
  private long refreshTokenTtlMs;

  @NotNull(message = "refresh-token-max-lifetime-ms must not be null")
  private long refreshTokenMaxLifetimeMs;
}
