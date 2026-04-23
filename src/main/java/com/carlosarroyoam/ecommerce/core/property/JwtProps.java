package com.carlosarroyoam.ecommerce.core.property;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "application.jwt")
@Getter
@Setter
public class JwtProps {
  @NotNull(message = "secret must not be null")
  private String secret;

  @NotNull(message = "access-token-ttl-ms must not be null")
  private long accessTokenTtlMs;

  @NotNull(message = "refresh-token-ttl-ms must not be null")
  private long refreshTokenTtlMs;
}
