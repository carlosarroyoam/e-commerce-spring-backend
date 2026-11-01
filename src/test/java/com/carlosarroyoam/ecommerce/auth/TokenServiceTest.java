package com.carlosarroyoam.ecommerce.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.ecommerce.auth.principal.AuthPrincipal;
import com.carlosarroyoam.ecommerce.auth.principal.PrincipalType;
import com.carlosarroyoam.ecommerce.core.property.JwtProps;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {
  private static final long ACCESS_TOKEN_TTL_MS = 300_000L;

  @Mock private JwtEncoder jwtEncoder;
  @Spy private Clock clock = Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneOffset.UTC);

  private TokenService tokenService;
  private AuthPrincipal principal;

  @BeforeEach
  void setUp() {
    JwtProps jwtProps = new JwtProps();
    jwtProps.setAccessTokenTtlMs(ACCESS_TOKEN_TTL_MS);
    jwtProps.setRefreshTokenTtlMs(86_400_000L);
    jwtProps.setRefreshTokenMaxLifetimeMs(2_592_000_000L);

    tokenService = new TokenService(jwtProps, jwtEncoder, clock);

    principal =
        AuthPrincipal.builder()
            .id(1L)
            .fullName("Carlos Alberto Arroyo Martínez")
            .firstName("Carlos Alberto")
            .lastName("Arroyo Martínez")
            .email("carlos.arroyo@e-commerce.com")
            .passwordHash("hash")
            .status("ACTIVE")
            .principalType(PrincipalType.STAFF)
            .roles(Set.of("ADMIN"))
            .build();
  }

  @Test
  @DisplayName("Given a principal, when generate access token, then encodes the expected claims")
  void givenPrincipal_whenGenerateAccessToken_thenEncodesExpectedClaims() {
    Instant now = Instant.now(clock);
    Jwt jwt =
        Jwt.withTokenValue("encoded-access-token")
            .header("alg", "RS256")
            .subject("1")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(300))
            .build();
    when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

    String accessToken = tokenService.generateAccessToken(principal);

    assertThat(accessToken).isEqualTo("encoded-access-token");
    ArgumentCaptor<JwtEncoderParameters> captor =
        ArgumentCaptor.forClass(JwtEncoderParameters.class);
    verify(jwtEncoder).encode(captor.capture());
    JwtClaimsSet claims = captor.getValue().getClaims();
    assertThat(claims.getClaimAsString("iss")).isEqualTo("self");
    assertThat(claims.getSubject()).isEqualTo("1");
    assertThat(claims.getClaimAsString("email")).isEqualTo("carlos.arroyo@e-commerce.com");
    assertThat(claims.getClaimAsString("name")).isEqualTo("Carlos Alberto Arroyo Martínez");
    assertThat(claims.getClaimAsString("given_name")).isEqualTo("Carlos Alberto");
    assertThat(claims.getClaimAsString("family_name")).isEqualTo("Arroyo Martínez");
    assertThat(claims.getClaimAsString("principal_type")).isEqualTo("STAFF");
    assertThat(claims.getClaimAsStringList("roles")).containsExactly("ADMIN");
    assertThat(claims.getIssuedAt()).isEqualTo(now);
    assertThat(claims.getExpiresAt()).isEqualTo(now.plus(ACCESS_TOKEN_TTL_MS, ChronoUnit.MILLIS));
  }

  @Test
  @DisplayName("Given a customer principal, when generate access token, then sets customer type")
  void givenCustomerPrincipal_whenGenerateAccessToken_thenSetsCustomerPrincipalType() {
    AuthPrincipal customer =
        AuthPrincipal.builder()
            .id(7L)
            .fullName("José García Hernández")
            .firstName("José")
            .lastName("García Hernández")
            .email("jose.garcia@example.com")
            .passwordHash("hash")
            .status("ACTIVE")
            .principalType(PrincipalType.CUSTOMER)
            .roles(Set.of("CUSTOMER"))
            .build();
    Jwt jwt = Jwt.withTokenValue("encoded").header("alg", "RS256").subject("7").build();
    when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

    tokenService.generateAccessToken(customer);

    ArgumentCaptor<JwtEncoderParameters> captor =
        ArgumentCaptor.forClass(JwtEncoderParameters.class);
    verify(jwtEncoder).encode(captor.capture());
    JwtClaimsSet claims = captor.getValue().getClaims();
    assertThat(claims.getSubject()).isEqualTo("7");
    assertThat(claims.getClaimAsString("principal_type")).isEqualTo("CUSTOMER");
    assertThat(claims.getClaimAsStringList("roles")).containsExactly("CUSTOMER");
  }

  @Test
  @DisplayName("Given two calls, when generate refresh token, then returns distinct valid UUIDs")
  void givenTwoCalls_whenGenerateRefreshToken_thenReturnsDistinctUuids() {
    String first = tokenService.generateRefreshToken();
    String second = tokenService.generateRefreshToken();

    assertThat(UUID.fromString(first)).hasToString(first);
    assertThat(UUID.fromString(second)).hasToString(second);
    assertThat(first).isNotEqualTo(second);
  }
}
