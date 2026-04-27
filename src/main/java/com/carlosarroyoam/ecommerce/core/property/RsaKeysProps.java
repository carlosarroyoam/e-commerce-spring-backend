package com.carlosarroyoam.ecommerce.core.property;

import jakarta.validation.constraints.NotNull;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "application.rsa")
@Getter
@Setter
public class RsaKeysProps {
  @NotNull(message = "private-key must not be null")
  private RSAPrivateKey privateKey;

  @NotNull(message = "public-key must not be null")
  private RSAPublicKey publicKey;
}
