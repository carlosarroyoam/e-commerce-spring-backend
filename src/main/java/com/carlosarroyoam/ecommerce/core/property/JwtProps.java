package com.carlosarroyoam.ecommerce.core.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProps {
  private String secret;
  private long accessTokenTtlMs;
  private long refreshTokenTtlMs;
}
