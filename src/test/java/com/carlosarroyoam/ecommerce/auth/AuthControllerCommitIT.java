package com.carlosarroyoam.ecommerce.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.ecommerce.auth.entity.RefreshToken;
import com.carlosarroyoam.ecommerce.support.AbstractIntegrationTest;
import jakarta.servlet.http.Cookie;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

// Deliberadamente sin @Transactional: un rollback de test enmascararía si el borrado del refresh
// token inválido se confirma pese a la excepción (noRollbackFor), que es lo que se verifica aquí.
@SpringBootTest
@ActiveProfiles("test")
class AuthControllerCommitIT extends AbstractIntegrationTest {
  private static final String LOGIN_BODY =
      """
      {"email": "carlos.arroyo@e-commerce.com", "password": "secret123", "device_id": "device-1"}
      """;
  private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private RefreshTokenRepository refreshTokenRepository;
  @Autowired private Clock clock;

  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    mockMvc =
        MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
  }

  @AfterEach
  void tearDown() {
    refreshTokenRepository.deleteAll();
  }

  private Cookie login() throws Exception {
    return mockMvc
        .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getCookie(REFRESH_TOKEN_COOKIE);
  }

  private static UUID tokenId(Cookie cookie) {
    return UUID.fromString(cookie.getValue().split("\\.", 2)[0]);
  }

  @Test
  @DisplayName(
      "POST /auth/refresh-token - Given a cookie with a wrong raw value, when refresh token, then"
          + " returns unauthorized and the deletion is committed")
  void givenCookieWithWrongRawValue_whenRefreshToken_thenReturnsUnauthorizedAndCommitsDeletion()
      throws Exception {
    Cookie loginCookie = login();
    UUID id = tokenId(loginCookie);
    Cookie tampered = new Cookie(REFRESH_TOKEN_COOKIE, id + ".wrong-raw-token");

    mockMvc
        .perform(post("/auth/refresh-token").cookie(tampered).with(csrf()))
        .andExpect(status().isUnauthorized())
        .andExpect(
            jsonPath("$.detail").value("The provided refresh token is not valid or has expired"));

    assertThat(refreshTokenRepository.existsById(id)).isFalse();
  }

  @Test
  @DisplayName(
      "POST /auth/refresh-token - Given a session past its max lifetime, when refresh token, then"
          + " returns unauthorized and the deletion is committed")
  void givenSessionPastMaxLifetime_whenRefreshToken_thenReturnsUnauthorizedAndCommitsDeletion()
      throws Exception {
    Cookie loginCookie = login();
    UUID id = tokenId(loginCookie);
    RefreshToken stored = refreshTokenRepository.findById(id).orElseThrow();
    stored.setCreatedAt(LocalDateTime.now(clock).minusDays(1));
    refreshTokenRepository.save(stored);

    mockMvc
        .perform(post("/auth/refresh-token").cookie(loginCookie).with(csrf()))
        .andExpect(status().isUnauthorized());

    assertThat(refreshTokenRepository.existsById(id)).isFalse();
  }

  @Test
  @DisplayName(
      "POST /auth/refresh-token - Given a valid cookie, when refresh token, then keeps the row and"
          + " rotates the hash")
  void givenValidCookie_whenRefreshToken_thenKeepsRowAndRotatesHash() throws Exception {
    Cookie loginCookie = login();
    UUID id = tokenId(loginCookie);
    String hashBefore = refreshTokenRepository.findById(id).orElseThrow().getTokenHash();

    mockMvc
        .perform(post("/auth/refresh-token").cookie(loginCookie).with(csrf()))
        .andExpect(status().isOk());

    RefreshToken after = refreshTokenRepository.findById(id).orElseThrow();
    assertThat(after.getTokenHash()).isNotEqualTo(hashBefore);
    assertThat(after.getLastUsedAt()).isNotNull();
  }
}
