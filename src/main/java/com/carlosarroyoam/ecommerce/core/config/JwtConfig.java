package com.carlosarroyoam.ecommerce.core.config;

import com.carlosarroyoam.ecommerce.core.property.RsaKeysProps;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtConfig {
  @Bean
  JwtEncoder jwtEncoder(RsaKeysProps rsaKeys) {
    RSAKey rsaKey =
        new RSAKey.Builder(rsaKeys.getPublicKey()).privateKey(rsaKeys.getPrivateKey()).build();
    JWKSet jwkSet = new JWKSet(rsaKey);
    ImmutableJWKSet<SecurityContext> immutableJWKSet = new ImmutableJWKSet<>(jwkSet);

    return new NimbusJwtEncoder(immutableJWKSet);
  }

  @Bean
  JwtDecoder jwtDecoder(RsaKeysProps rsaKeys) {
    return NimbusJwtDecoder.withPublicKey(rsaKeys.getPublicKey()).build();
  }
}
