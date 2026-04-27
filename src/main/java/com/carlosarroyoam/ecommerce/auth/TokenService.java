package com.carlosarroyoam.ecommerce.auth;

import com.carlosarroyoam.ecommerce.auth.principal.AuthPrincipal;
import com.carlosarroyoam.ecommerce.auth.principal.PrincipalType;
import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.property.JwtProps;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
  private static final Logger log = LoggerFactory.getLogger(TokenService.class);
  private final long accessTokenTtlMs;
  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;

  public TokenService(JwtProps jwtProps, JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
    this.accessTokenTtlMs = jwtProps.getAccessTokenTtlMs();
    this.jwtEncoder = jwtEncoder;
    this.jwtDecoder = jwtDecoder;
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
            .claim("principal_type", principal.getPrincipalType())
            .claim(
                "roles",
                principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
            .claim(
                "scope",
                principal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(" ")))
            .issuedAt(now)
            .expiresAt(now.plus(accessTokenTtlMs, ChronoUnit.MILLIS))
            .build();

    return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  public String generateRefreshToken() {
    return UUID.randomUUID().toString();
  }

  public boolean isValid(String token) {
    try {
      jwtDecoder.decode(token);
      return true;
    } catch (Exception exception) {
      log.error(AppMessages.JWT_AUTHORIZATION_TOKEN_IS_NOT_VALID);
      throw new BadJwtException(AppMessages.JWT_AUTHORIZATION_TOKEN_IS_NOT_VALID);
    }
  }

  public PrincipalType extractPrincipalType(String token) {
    Jwt jwtToken = jwtDecoder.decode(token);
    return PrincipalType.valueOf(jwtToken.getClaim("principal_type"));
  }

  public String extractEmail(String token) {
    Jwt jwtToken = jwtDecoder.decode(token);
    return jwtToken.getClaimAsString("email");
  }
}
