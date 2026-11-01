package com.carlosarroyoam.ecommerce.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.ecommerce.auth.entity.RefreshToken;
import com.carlosarroyoam.ecommerce.auth.principal.AuthPrincipal;
import com.carlosarroyoam.ecommerce.auth.principal.PrincipalType;
import com.carlosarroyoam.ecommerce.core.constant.MessageKeys;
import com.carlosarroyoam.ecommerce.core.exception.UnauthorizedException;
import com.carlosarroyoam.ecommerce.core.property.JwtProps;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {
  private static final long REFRESH_TOKEN_TTL_MS = 86_400_000L;
  private static final long REFRESH_TOKEN_MAX_LIFETIME_MS = 2_592_000_000L;
  private static final String DEVICE_ID = "device-1";

  @Mock private PasswordEncoder passwordEncoder;
  @Mock private RefreshTokenRepository refreshTokenRepository;
  @Spy private Clock clock = Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneOffset.UTC);

  private RefreshTokenService refreshTokenService;
  private LocalDateTime now;
  private UUID refreshTokenId;
  private AuthPrincipal principal;
  private RefreshToken refreshToken;

  @BeforeEach
  void setUp() {
    JwtProps jwtProps = new JwtProps();
    jwtProps.setAccessTokenTtlMs(300_000L);
    jwtProps.setRefreshTokenTtlMs(REFRESH_TOKEN_TTL_MS);
    jwtProps.setRefreshTokenMaxLifetimeMs(REFRESH_TOKEN_MAX_LIFETIME_MS);

    refreshTokenService =
        new RefreshTokenService(passwordEncoder, jwtProps, refreshTokenRepository, clock);

    now = LocalDateTime.now(clock);
    refreshTokenId = UUID.fromString("11111111-2222-3333-4444-555555555555");

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

    refreshToken =
        RefreshToken.builder()
            .id(refreshTokenId)
            .tokenHash("old-hash")
            .deviceId(DEVICE_ID)
            .principalId(1L)
            .principalType(PrincipalType.STAFF)
            .expiresOn(now.plusHours(12))
            .lastUsedAt(null)
            .createdAt(now.minusHours(1))
            .updatedAt(now.minusHours(1))
            .build();
  }

  @Test
  @DisplayName("Given existing refresh token, when find by id, then returns it")
  void givenExistingRefreshToken_whenFindById_thenReturnsRefreshToken() {
    when(refreshTokenRepository.findById(refreshTokenId)).thenReturn(Optional.of(refreshToken));

    RefreshToken found = refreshTokenService.findById(refreshTokenId);

    assertThat(found).isSameAs(refreshToken);
  }

  @Test
  @DisplayName("Given non existing refresh token, when find by id, then throws unauthorized")
  void givenNonExistingRefreshToken_whenFindById_thenThrowsUnauthorizedException() {
    when(refreshTokenRepository.findById(refreshTokenId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> refreshTokenService.findById(refreshTokenId))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage(MessageKeys.REFRESH_TOKEN_IS_NOT_VALID_EXCEPTION);
  }

  @Test
  @DisplayName("Given no token for the device, when save, then creates a new refresh token")
  void givenNoTokenForDevice_whenSave_thenCreatesRefreshToken() {
    when(refreshTokenRepository.findByDeviceIdAndPrincipalTypeAndPrincipalId(
            DEVICE_ID, PrincipalType.STAFF, 1L))
        .thenReturn(Optional.empty());
    when(passwordEncoder.encode("raw-token")).thenReturn("encoded-hash");
    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenAnswer(AdditionalAnswers.returnsFirstArg());

    RefreshToken saved = refreshTokenService.save(principal, DEVICE_ID, "raw-token");

    ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
    verify(refreshTokenRepository).save(captor.capture());
    RefreshToken persisted = captor.getValue();
    assertThat(saved).isSameAs(persisted);
    assertThat(persisted.getId()).isNull();
    assertThat(persisted.getDeviceId()).isEqualTo(DEVICE_ID);
    assertThat(persisted.getPrincipalId()).isEqualTo(1L);
    assertThat(persisted.getPrincipalType()).isEqualTo(PrincipalType.STAFF);
    assertThat(persisted.getTokenHash()).isEqualTo("encoded-hash");
    assertThat(persisted.getExpiresOn())
        .isEqualTo(now.plus(REFRESH_TOKEN_TTL_MS, ChronoUnit.MILLIS));
    assertThat(persisted.getLastUsedAt()).isNull();
    assertThat(persisted.getCreatedAt()).isEqualTo(now);
    assertThat(persisted.getUpdatedAt()).isEqualTo(now);
  }

  @Test
  @DisplayName("Given token for the device, when save, then updates the existing refresh token")
  void givenTokenForDevice_whenSave_thenUpdatesExistingRefreshToken() {
    refreshToken.setLastUsedAt(now.minusMinutes(5));
    when(refreshTokenRepository.findByDeviceIdAndPrincipalTypeAndPrincipalId(
            DEVICE_ID, PrincipalType.STAFF, 1L))
        .thenReturn(Optional.of(refreshToken));
    when(passwordEncoder.encode("raw-token")).thenReturn("encoded-hash");
    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenAnswer(AdditionalAnswers.returnsFirstArg());

    RefreshToken saved = refreshTokenService.save(principal, DEVICE_ID, "raw-token");

    assertThat(saved).isSameAs(refreshToken);
    assertThat(saved.getId()).isEqualTo(refreshTokenId);
    assertThat(saved.getTokenHash()).isEqualTo("encoded-hash");
    assertThat(saved.getExpiresOn()).isEqualTo(now.plus(REFRESH_TOKEN_TTL_MS, ChronoUnit.MILLIS));
    assertThat(saved.getLastUsedAt()).isNull();
    assertThat(saved.getCreatedAt()).isEqualTo(now);
    assertThat(saved.getUpdatedAt()).isEqualTo(now);
  }

  @Test
  @DisplayName("Given valid refresh token, when rotate, then updates hash and expiration")
  void givenValidRefreshToken_whenRotate_thenUpdatesRefreshToken() {
    LocalDateTime createdAt = refreshToken.getCreatedAt();
    when(refreshTokenRepository.findById(refreshTokenId)).thenReturn(Optional.of(refreshToken));
    when(passwordEncoder.matches("current-raw", "old-hash")).thenReturn(true);
    when(passwordEncoder.encode("new-raw")).thenReturn("new-hash");
    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenAnswer(AdditionalAnswers.returnsFirstArg());

    RefreshToken rotated = refreshTokenService.rotate(refreshTokenId, "current-raw", "new-raw");

    assertThat(rotated).isSameAs(refreshToken);
    assertThat(rotated.getTokenHash()).isEqualTo("new-hash");
    assertThat(rotated.getLastUsedAt()).isEqualTo(now);
    assertThat(rotated.getExpiresOn()).isEqualTo(now.plus(REFRESH_TOKEN_TTL_MS, ChronoUnit.MILLIS));
    assertThat(rotated.getUpdatedAt()).isEqualTo(now);
    assertThat(rotated.getCreatedAt()).isEqualTo(createdAt);
    verify(refreshTokenRepository).save(refreshToken);
    verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
  }

  @Test
  @DisplayName("Given non existing refresh token, when rotate, then throws unauthorized")
  void givenNonExistingRefreshToken_whenRotate_thenThrowsUnauthorizedException() {
    when(refreshTokenRepository.findById(refreshTokenId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> refreshTokenService.rotate(refreshTokenId, "current-raw", "new-raw"))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage(MessageKeys.REFRESH_TOKEN_IS_NOT_VALID_EXCEPTION);

    verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
  }

  @Test
  @DisplayName("Given wrong token hash, when rotate, then throws unauthorized and deletes token")
  void givenWrongTokenHash_whenRotate_thenThrowsUnauthorizedAndDeletesToken() {
    when(refreshTokenRepository.findById(refreshTokenId)).thenReturn(Optional.of(refreshToken));
    when(passwordEncoder.matches("wrong-raw", "old-hash")).thenReturn(false);

    assertThatThrownBy(() -> refreshTokenService.rotate(refreshTokenId, "wrong-raw", "new-raw"))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage(MessageKeys.REFRESH_TOKEN_IS_NOT_VALID_EXCEPTION);

    verify(refreshTokenRepository).delete(refreshToken);
    verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
  }

  @Test
  @DisplayName(
      "Given session older than max lifetime, when rotate, then throws unauthorized and deletes"
          + " token")
  void givenSessionPastMaxLifetime_whenRotate_thenThrowsUnauthorizedAndDeletesToken() {
    refreshToken.setCreatedAt(
        now.minus(REFRESH_TOKEN_MAX_LIFETIME_MS, ChronoUnit.MILLIS).minusSeconds(1));
    when(refreshTokenRepository.findById(refreshTokenId)).thenReturn(Optional.of(refreshToken));
    when(passwordEncoder.matches("current-raw", "old-hash")).thenReturn(true);

    assertThatThrownBy(() -> refreshTokenService.rotate(refreshTokenId, "current-raw", "new-raw"))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage(MessageKeys.REFRESH_TOKEN_IS_NOT_VALID_EXCEPTION);

    verify(refreshTokenRepository).delete(refreshToken);
    verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
  }

  @Test
  @DisplayName(
      "Given token expired by inactivity, when rotate, then throws unauthorized without deleting")
  void givenTokenExpiredByInactivity_whenRotate_thenThrowsUnauthorizedWithoutDeleting() {
    refreshToken.setExpiresOn(now.minusSeconds(1));
    when(refreshTokenRepository.findById(refreshTokenId)).thenReturn(Optional.of(refreshToken));
    when(passwordEncoder.matches("current-raw", "old-hash")).thenReturn(true);

    assertThatThrownBy(() -> refreshTokenService.rotate(refreshTokenId, "current-raw", "new-raw"))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage(MessageKeys.REFRESH_TOKEN_IS_NOT_VALID_EXCEPTION);

    verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
  }

  @Test
  @DisplayName("Given matching hash, when revoke, then deletes the refresh token")
  void givenMatchingHash_whenRevoke_thenDeletesRefreshToken() {
    when(refreshTokenRepository.findById(refreshTokenId)).thenReturn(Optional.of(refreshToken));
    when(passwordEncoder.matches("current-raw", "old-hash")).thenReturn(true);

    refreshTokenService.revoke(refreshTokenId, "current-raw");

    verify(refreshTokenRepository).delete(refreshToken);
  }

  @Test
  @DisplayName("Given wrong hash, when revoke, then does not delete the refresh token")
  void givenWrongHash_whenRevoke_thenDoesNotDelete() {
    when(refreshTokenRepository.findById(refreshTokenId)).thenReturn(Optional.of(refreshToken));
    when(passwordEncoder.matches("wrong-raw", "old-hash")).thenReturn(false);

    refreshTokenService.revoke(refreshTokenId, "wrong-raw");

    verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
  }

  @Test
  @DisplayName("Given non existing refresh token, when revoke, then does nothing")
  void givenNonExistingRefreshToken_whenRevoke_thenDoesNothing() {
    when(refreshTokenRepository.findById(refreshTokenId)).thenReturn(Optional.empty());

    refreshTokenService.revoke(refreshTokenId, "current-raw");

    verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
  }
}
