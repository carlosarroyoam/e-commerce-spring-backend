package com.carlosarroyoam.ecommerce.core.config;

import com.carlosarroyoam.ecommerce.core.property.RsaKeyProps;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Configuración de codificación y decodificación de JWT (RS256) usando el par de llaves RSA de
 * {@link RsaKeyProps}, para el esquema de autenticación autoemitida y autoválidada (sin IdP
 * externo).
 */
@Configuration
public class JwtConfig {
  /**
   * Codificador de JWT usado para emitir access tokens firmados con la llave privada RSA.
   *
   * @param rsaKeyProps el par de llaves RSA configurado
   * @return el {@link JwtEncoder} resultante
   */
  @Bean
  JwtEncoder jwtEncoder(RsaKeyProps rsaKeyProps) {
    RSAKey rsaKey =
        new RSAKey.Builder(rsaKeyProps.getPublicKey())
            .privateKey(rsaKeyProps.getPrivateKey())
            .build();

    JWKSet jwkSet = new JWKSet(rsaKey);
    ImmutableJWKSet<SecurityContext> immutableJWKSet = new ImmutableJWKSet<>(jwkSet);
    return new NimbusJwtEncoder(immutableJWKSet);
  }

  /**
   * Decodificador de JWT usado por el recurso OAuth2 para verificar los access tokens con la
   * llave pública RSA, validando además el emisor ({@code self}).
   *
   * @param rsaKeyProps el par de llaves RSA configurado
   * @return el {@link JwtDecoder} resultante
   */
  @Bean
  JwtDecoder jwtDecoder(RsaKeyProps rsaKeyProps) {
    NimbusJwtDecoder jwtDecoder =
        NimbusJwtDecoder.withPublicKey(rsaKeyProps.getPublicKey()).build();
    jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("self"));
    return jwtDecoder;
  }
}
