package com.carlosarroyoam.ecommerce.core.property;

import jakarta.validation.constraints.NotNull;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Propiedades de configuración del par de llaves RSA bajo el prefijo {@code app.rsa},
 * usadas para firmar y verificar los JWT emitidos por la API.
 */
@Component
@ConfigurationProperties(prefix = "app.rsa")
@Validated
@Getter
@Setter
public class RsaKeyProps {
  @NotNull(message = "app.rsa.private-key must not be null")
  private RSAPrivateKey privateKey;

  @NotNull(message = "app.rsa.public-key must not be null")
  private RSAPublicKey publicKey;
}
