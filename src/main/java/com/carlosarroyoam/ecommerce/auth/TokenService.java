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

@Service
public class TokenService {
  private final long accessTokenTtlMs;
  private final JwtEncoder jwtEncoder;

  public TokenService(JwtProps jwtProps, JwtEncoder jwtEncoder) {
    this.accessTokenTtlMs = jwtProps.getAccessTokenTtlMs();
    this.jwtEncoder = jwtEncoder;
  }

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

  public String generateRefreshToken() {
    return UUID.randomUUID().toString();
  }
}
