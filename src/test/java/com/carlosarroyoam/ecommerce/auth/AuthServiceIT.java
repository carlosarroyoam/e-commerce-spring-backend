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
 * Integración de {@link AuthService#refreshToken} contra MySQL real (Testcontainers): verifica que
 * la transacción externa no revierta la eliminación del refresh token inválido.
 *
 * <p>La clase no es {@code @Transactional} a propósito: un rollback de test enmascararía si el
 * borrado se confirma o no.
 */
@SpringBootTest
class AuthServiceIT extends AbstractIntegrationTest {
  private static final long ADMIN_STAFF_ID = 1L;
  private static final String RAW_TOKEN = "raw-refresh-token";

  @Autowired private AuthService authService;
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
      "Given a cookie with a wrong raw value, when refresh, then throws 401 and deletes the row")
  void givenHashMismatch_whenRefreshToken_thenThrowsUnauthorizedAndDeletesRow() {
    RefreshToken stored = persistToken();
    String cookie = stored.getId() + ".wrong-raw-token";

    assertThatThrownBy(() -> authService.refreshToken(cookie))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage(MessageKeys.REFRESH_TOKEN_IS_NOT_VALID_EXCEPTION);

    assertThat(refreshTokenRepository.existsById(stored.getId())).isFalse();
  }

  @Test
  @DisplayName(
      "Given a valid cookie, when refresh, then rotates the token and keeps the row")
  void givenValidCookie_whenRefreshToken_thenRotatesAndKeepsRow() {
    RefreshToken stored = persistToken();

    var response = authService.refreshToken(stored.getId() + "." + RAW_TOKEN);

    assertThat(response.getId()).isEqualTo(ADMIN_STAFF_ID);
    assertThat(response.getRefreshToken()).startsWith(stored.getId() + ".");
    assertThat(refreshTokenRepository.existsById(stored.getId())).isTrue();
  }

  private RefreshToken persistToken() {
    LocalDateTime now = LocalDateTime.now();
    return refreshTokenRepository.save(
        RefreshToken.builder()
            .tokenHash(passwordEncoder.encode(RAW_TOKEN))
            .deviceId(UUID.randomUUID().toString())
            .principalId(ADMIN_STAFF_ID)
            .principalType(PrincipalType.STAFF)
            .expiresOn(now.plusHours(1))
            .createdAt(now)
            .updatedAt(now)
            .build());
  }
}
