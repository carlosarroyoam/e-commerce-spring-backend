package com.carlosarroyoam.ecommerce.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carlosarroyoam.ecommerce.auth.entity.RefreshToken;
import com.carlosarroyoam.ecommerce.auth.principal.PrincipalType;
import com.carlosarroyoam.ecommerce.core.constant.CommonConstants;
import com.carlosarroyoam.ecommerce.core.constant.MessageKeys;
import com.carlosarroyoam.ecommerce.core.exception.UnauthorizedException;
import com.carlosarroyoam.ecommerce.support.AbstractIntegrationTest;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Integracion de {@link RefreshTokenService#rotate} contra MySQL real (Testcontainers).
 *
 * <p>La clase no es {@code @Transactional} a proposito: un rollback de test enmascararia si el
 * borrado del token invalido se confirma o no.
 */
@SpringBootTest
class RefreshTokenServiceIT extends AbstractIntegrationTest {
  private static final String RAW_TOKEN = "raw-refresh-token";
  private static final String NEW_RAW_TOKEN = "new-raw-refresh-token";

  @Autowired private RefreshTokenService refreshTokenService;
  @Autowired private RefreshTokenRepository refreshTokenRepository;

  @Autowired
  @Qualifier("refreshTokenPasswordEncoder")
  private PasswordEncoder passwordEncoder;

  @AfterEach
  void tearDown() {
    refreshTokenRepository.deleteAll();
  }

  @Test
  @DisplayName(
      "Given a valid refresh token, when rotate, then replaces the hash and keeps the row")
  void givenValidToken_whenRotate_thenReplacesHashAndKeepsRow() {
    RefreshToken stored = persistToken(LocalDateTime.now(), LocalDateTime.now().plusHours(1));

    RefreshToken rotated = refreshTokenService.rotate(stored.getId(), RAW_TOKEN, NEW_RAW_TOKEN);

    assertThat(rotated.getLastUsedAt()).isNotNull();
    assertThat(passwordEncoder.matches(NEW_RAW_TOKEN, rotated.getTokenHash())).isTrue();
    assertThat(refreshTokenRepository.existsById(stored.getId())).isTrue();
  }

  @Test
  @DisplayName(
      "Given a token with a wrong raw value, when rotate, then throws 401 and deletes the row")
  void givenHashMismatch_whenRotate_thenThrowsUnauthorizedAndDeletesRow() {
    RefreshToken stored = persistToken(LocalDateTime.now(), LocalDateTime.now().plusHours(1));
    UUID id = stored.getId();

    assertThatThrownBy(() -> refreshTokenService.rotate(id, "wrong-raw-token", NEW_RAW_TOKEN))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage(MessageKeys.REFRESH_TOKEN_IS_NOT_VALID_EXCEPTION);

    assertThat(refreshTokenRepository.existsById(id)).isFalse();
  }

  @Test
  @DisplayName(
      "Given a session past its max lifetime, when rotate, then throws 401 and deletes the row")
  void givenSessionExpired_whenRotate_thenThrowsUnauthorizedAndDeletesRow() {
    RefreshToken stored =
        persistToken(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusHours(1));
    UUID id = stored.getId();

    assertThatThrownBy(() -> refreshTokenService.rotate(id, RAW_TOKEN, NEW_RAW_TOKEN))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage(MessageKeys.REFRESH_TOKEN_IS_NOT_VALID_EXCEPTION);

    assertThat(refreshTokenRepository.existsById(id)).isFalse();
  }

  @Test
  @DisplayName(
      "Given a token expired by inactivity, when rotate, then throws 401 and keeps the row")
  void givenTokenExpired_whenRotate_thenThrowsUnauthorizedAndKeepsRow() {
    RefreshToken stored = persistToken(LocalDateTime.now(), LocalDateTime.now().minusMinutes(1));
    UUID id = stored.getId();

    assertThatThrownBy(() -> refreshTokenService.rotate(id, RAW_TOKEN, NEW_RAW_TOKEN))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage(MessageKeys.REFRESH_TOKEN_IS_NOT_VALID_EXCEPTION);

    assertThat(refreshTokenRepository.existsById(id)).isTrue();
  }

  private RefreshToken persistToken(LocalDateTime createdAt, LocalDateTime expiresOn) {
    return refreshTokenRepository.save(
        RefreshToken.builder()
            .tokenHash(passwordEncoder.encode(RAW_TOKEN))
            .deviceId(UUID.randomUUID().toString())
            .principalId(1L)
            .principalType(PrincipalType.STAFF)
            .expiresOn(expiresOn)
            .createdAt(createdAt)
            .updatedAt(createdAt)
            .build());
  }
}
