package com.carlosarroyoam.ecommerce.core.config;

import com.carlosarroyoam.ecommerce.core.property.JwtProps;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtConfig {
  @Bean
  JwtEncoder jwtEncoder(JwtProps jwtProps) {
    return new NimbusJwtEncoder(new ImmutableSecret<>(jwtProps.getSecret().getBytes()));
  }

  @Bean
  JwtDecoder jwtDecoder(JwtProps jwtProps) {
    byte[] bytes = jwtProps.getSecret().getBytes();
    SecretKeySpec originalKey = new SecretKeySpec(bytes, 0, bytes.length, "HmacSHA256");
    return NimbusJwtDecoder.withSecretKey(originalKey).macAlgorithm(MacAlgorithm.HS256).build();
  }
}
