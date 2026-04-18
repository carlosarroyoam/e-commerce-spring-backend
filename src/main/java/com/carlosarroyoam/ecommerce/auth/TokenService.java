package com.carlosarroyoam.ecommerce.auth;

import com.carlosarroyoam.ecommerce.auth.principal.AuthPrincipal;
import com.carlosarroyoam.ecommerce.auth.principal.PrincipalType;
import com.carlosarroyoam.ecommerce.core.property.JwtProps;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
  private final SecretKey secretKey;
  private final long accessTokenTtlMs;

  public TokenService(JwtProps jwtProps) {
    this.secretKey = Keys.hmacShaKeyFor(jwtProps.getSecret().getBytes(StandardCharsets.UTF_8));
    this.accessTokenTtlMs = jwtProps.getAccessTokenTtlMs();
  }

  public String generateAccessToken(AuthPrincipal principal) {
    return generateAccessToken(principal, Map.of());
  }

  public String generateAccessToken(AuthPrincipal principal, Map<String, Object> extraClaims) {
    Instant now = Instant.now();

    return Jwts.builder()
        .issuer("self")
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusMillis(accessTokenTtlMs)))
        .subject(principal.getId().toString())
        .claim("name", principal.getFullName())
        .claim("given_name", principal.getFirstName())
        .claim("family_name", principal.getLastName())
        .claim("email", principal.getEmail())
        .claim("type", principal.getPrincipalType())
        .claim("roles",
            principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
        .claims(extraClaims)
        .signWith(secretKey)
        .compact();
  }

  public String generateRefreshToken() {
    return UUID.randomUUID().toString();
  }

  public boolean isValid(String token, UserDetails principal) {
    try {
      String email = extractEmail(token);
      return email.equals(principal.getUsername()) && !isExpired(token);
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public boolean isExpired(String token) {
    return parseClaims(token).getExpiration().before(new Date());
  }

  public String extractSubject(String token) {
    return parseClaims(token).getSubject();
  }

  public String extractEmail(String token) {
    return parseClaims(token).get("email", String.class);
  }

  public PrincipalType extractType(String token) {
    String raw = parseClaims(token).get("type", String.class);
    return PrincipalType.valueOf(raw);
  }

  public <T> T extractClaim(String token, String name, Class<T> type) {
    return parseClaims(token).get(name, type);
  }

  private Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }
}
