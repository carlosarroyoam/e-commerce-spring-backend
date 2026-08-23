package com.carlosarroyoam.ecommerce.auth;

import com.carlosarroyoam.ecommerce.auth.principal.AuthPrincipal;
import com.carlosarroyoam.ecommerce.core.property.JwtProps;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/** Genera los access tokens JWT (firmados con RSA) y los valores crudos de refresh token. */
@Service
public class TokenService {
  private final long accessTokenTtlMs;
  private final JwtEncoder jwtEncoder;

  public TokenService(JwtProps jwtProps, JwtEncoder jwtEncoder) {
    this.accessTokenTtlMs = jwtProps.getAccessTokenTtlMs();
    this.jwtEncoder = jwtEncoder;
  }

  /**
   * Genera un access token JWT firmado con RSA para el principal dado, incluyendo sus datos básicos
   * y roles como claims.
   *
   * @param principal el principal autenticado para el cual emitir el token
   * @return el access token JWT codificado
   */
  public String generateAccessToken(AuthPrincipal principal) {
    Instant now = Instant.now();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer("self")
            .subject(principal.getId().toString())
            .claim("email", principal.getEmail())
            .claim("name", principal.getFullName())
            .claim("given_name", principal.getFirstName())
            .claim("family_name", principal.getLastName())
            .claim("principal_type", principal.getPrincipalType().name())
            .claim("roles", principal.getRoles())
            .issuedAt(now)
            .expiresAt(now.plus(accessTokenTtlMs, ChronoUnit.MILLIS))
            .build();

    return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  /**
   * Genera un nuevo valor de refresh token sin hashear (un UUID aleatorio).
   *
   * @return el valor crudo del refresh token
   */
  public String generateRefreshToken() {
    return UUID.randomUUID().toString();
  }
}
