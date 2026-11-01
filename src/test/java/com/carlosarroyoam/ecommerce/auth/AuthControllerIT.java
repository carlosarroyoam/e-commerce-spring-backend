package com.carlosarroyoam.ecommerce.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.ecommerce.support.AbstractIntegrationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthControllerIT extends AbstractIntegrationTest {
  private static final String PASSWORD = "secret123";
  private static final String STAFF_EMAIL = "carlos.arroyo@e-commerce.com";
  private static final String CUSTOMER_EMAIL = "jose.garcia@example.com";
  private static final String INACTIVE_STAFF_EMAIL = "ana.rodriguez@e-comerce.com";
  private static final String PENDING_CUSTOMER_EMAIL = "maria.lopez@example.com";
  private static final String DEVICE_ID = "device-1";
  private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

  @Autowired private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    mockMvc =
        MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
  }

  private static String loginBody(String email, String password) {
    return """
    {"email": "%s", "password": "%s", "device_id": "%s"}
    """
        .formatted(email, password, DEVICE_ID);
  }

  private MvcResult login(String email) throws Exception {
    return mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(email, PASSWORD)))
        .andExpect(status().isOk())
        .andReturn();
  }

  @Test
  @DisplayName(
      "POST /auth/login - Given valid staff credentials, when login, then returns auth response"
          + " and refresh token cookie")
  void givenValidStaffCredentials_whenLogin_thenReturnsAuthResponseAndCookie() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody(STAFF_EMAIL, PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.full_name").value("Carlos Alberto Arroyo Martínez"))
            .andExpect(jsonPath("$.first_name").value("Carlos Alberto"))
            .andExpect(jsonPath("$.last_name").value("Arroyo Martínez"))
            .andExpect(jsonPath("$.email").value(STAFF_EMAIL))
            .andExpect(jsonPath("$.roles.length()").value(1))
            .andExpect(jsonPath("$.roles[0]").value("ADMIN"))
            .andExpect(jsonPath("$.access_token").isNotEmpty())
            .andExpect(jsonPath("$.refresh_token").doesNotExist())
            .andExpect(
                header().string(HttpHeaders.SET_COOKIE, containsString(REFRESH_TOKEN_COOKIE + "=")))
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
            .andReturn();

    Cookie refreshCookie = result.getResponse().getCookie(REFRESH_TOKEN_COOKIE);
    assertThat(refreshCookie).isNotNull();
    assertThat(refreshCookie.getValue()).contains(".");
    assertThat(refreshCookie.isHttpOnly()).isTrue();
  }

  @Test
  @DisplayName(
      "POST /auth/login - Given valid customer credentials, when login, then returns customer"
          + " auth response")
  void givenValidCustomerCredentials_whenLogin_thenReturnsAuthResponse() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody(CUSTOMER_EMAIL, PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value(CUSTOMER_EMAIL))
            .andExpect(jsonPath("$.roles.length()").value(1))
            .andExpect(jsonPath("$.roles[0]").value("CUSTOMER"))
            .andExpect(jsonPath("$.access_token").isNotEmpty())
            .andReturn();

    assertThat(result.getResponse().getCookie(REFRESH_TOKEN_COOKIE)).isNotNull();
  }

  @Test
  @DisplayName(
      "POST /auth/login - Given wrong password, when login, then returns unauthorized problem")
  void givenWrongPassword_whenLogin_thenReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(STAFF_EMAIL, "wrong-password")))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.detail").value("Bad credentials"))
        .andExpect(jsonPath("$.instance").value("/auth/login"));
  }

  @Test
  @DisplayName(
      "POST /auth/login - Given inactive staff user, when login, then returns unauthorized")
  void givenInactiveStaffUser_whenLogin_thenReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(INACTIVE_STAFF_EMAIL, PASSWORD)))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.title").value("Unauthorized"));
  }

  @Test
  @DisplayName("POST /auth/login - Given pending customer, when login, then returns unauthorized")
  void givenPendingCustomer_whenLogin_thenReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(PENDING_CUSTOMER_EMAIL, PASSWORD)))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.title").value("Unauthorized"));
  }

  @Test
  @DisplayName("POST /auth/login - Given empty body, when login, then returns validation errors")
  void givenEmptyBody_whenLogin_thenReturnsUnprocessableEntity() throws Exception {
    mockMvc
        .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.instance").value("/auth/login"))
        .andExpect(jsonPath("$.errors.email").isNotEmpty())
        .andExpect(jsonPath("$.errors.password").isNotEmpty())
        .andExpect(jsonPath("$.errors.deviceId").isNotEmpty());
  }

  @Test
  @DisplayName(
      "POST /auth/refresh-token - Given staff refresh token cookie, when refresh token, then"
          + " rotates the cookie")
  void givenStaffRefreshTokenCookie_whenRefreshToken_thenRotatesCookie() throws Exception {
    Cookie loginCookie = login(STAFF_EMAIL).getResponse().getCookie(REFRESH_TOKEN_COOKIE);

    MvcResult result =
        mockMvc
            .perform(post("/auth/refresh-token").cookie(loginCookie).with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value(STAFF_EMAIL))
            .andExpect(jsonPath("$.roles[0]").value("ADMIN"))
            .andExpect(jsonPath("$.access_token").isNotEmpty())
            .andExpect(jsonPath("$.refresh_token").doesNotExist())
            .andReturn();

    Cookie rotatedCookie = result.getResponse().getCookie(REFRESH_TOKEN_COOKIE);
    assertThat(rotatedCookie).isNotNull();
    assertThat(rotatedCookie.getValue()).isNotEqualTo(loginCookie.getValue());
    assertThat(rotatedCookie.getValue()).startsWith(loginCookie.getValue().split("\\.")[0] + ".");
  }

  @Test
  @DisplayName(
      "POST /auth/refresh-token - Given customer refresh token cookie, when refresh token, then"
          + " returns customer auth response")
  void givenCustomerRefreshTokenCookie_whenRefreshToken_thenReturnsCustomerAuthResponse()
      throws Exception {
    Cookie loginCookie = login(CUSTOMER_EMAIL).getResponse().getCookie(REFRESH_TOKEN_COOKIE);

    mockMvc
        .perform(post("/auth/refresh-token").cookie(loginCookie).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(CUSTOMER_EMAIL))
        .andExpect(jsonPath("$.roles[0]").value("CUSTOMER"))
        .andExpect(jsonPath("$.access_token").isNotEmpty())
        .andExpect(
            header().string(HttpHeaders.SET_COOKIE, containsString(REFRESH_TOKEN_COOKIE + "=")));
  }

  @Test
  @DisplayName(
      "POST /auth/refresh-token - Given no cookie, when refresh token, then returns unauthorized")
  void givenNoCookie_whenRefreshToken_thenReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(post("/auth/refresh-token").with(csrf()))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.detail").value("The refresh token is required"))
        .andExpect(jsonPath("$.instance").value("/auth/refresh-token"));
  }

  @Test
  @DisplayName(
      "POST /auth/refresh-token - Given malformed cookie, when refresh token, then returns"
          + " unauthorized")
  void givenMalformedCookie_whenRefreshToken_thenReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(
            post("/auth/refresh-token")
                .cookie(new Cookie(REFRESH_TOKEN_COOKIE, "not-a-valid-token"))
                .with(csrf()))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(
            jsonPath("$.detail").value("The provided refresh token is not valid or has expired"));
  }

  @Test
  @DisplayName(
      "POST /auth/refresh-token - Given unknown refresh token id, when refresh token, then returns"
          + " unauthorized")
  void givenUnknownRefreshTokenId_whenRefreshToken_thenReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(
            post("/auth/refresh-token")
                .cookie(
                    new Cookie(
                        REFRESH_TOKEN_COOKIE, "11111111-2222-3333-4444-555555555555.raw-token"))
                .with(csrf()))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
        .andExpect(
            jsonPath("$.detail").value("The provided refresh token is not valid or has expired"));
  }

  @Test
  @DisplayName(
      "POST /auth/refresh-token - Given no csrf token, when refresh token, then returns forbidden")
  void givenNoCsrfToken_whenRefreshToken_thenReturnsForbidden() throws Exception {
    Cookie loginCookie = login(STAFF_EMAIL).getResponse().getCookie(REFRESH_TOKEN_COOKIE);

    mockMvc
        .perform(post("/auth/refresh-token").cookie(loginCookie))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.instance").value("/auth/refresh-token"));
  }

  @Test
  @DisplayName(
      "POST /auth/logout - Given refresh token cookie, when logout, then clears cookie and"
          + " invalidates the token")
  void givenRefreshTokenCookie_whenLogout_thenInvalidatesRefreshToken() throws Exception {
    Cookie loginCookie = login(STAFF_EMAIL).getResponse().getCookie(REFRESH_TOKEN_COOKIE);

    mockMvc
        .perform(post("/auth/logout").cookie(loginCookie).with(csrf()))
        .andExpect(status().isNoContent())
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")))
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")));

    mockMvc
        .perform(post("/auth/refresh-token").cookie(loginCookie).with(csrf()))
        .andExpect(status().isUnauthorized())
        .andExpect(
            jsonPath("$.detail").value("The provided refresh token is not valid or has expired"));
  }

  @Test
  @DisplayName("POST /auth/logout - Given no cookie, when logout, then returns no content")
  void givenNoCookie_whenLogout_thenReturnsNoContent() throws Exception {
    mockMvc
        .perform(post("/auth/logout").with(csrf()))
        .andExpect(status().isNoContent())
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
  }

  @Test
  @DisplayName(
      "POST /auth/forgot-password - Given valid body, when forgot password, then returns not"
          + " implemented")
  void givenValidBody_whenForgotPassword_thenReturnsNotImplemented() throws Exception {
    mockMvc
        .perform(
            post("/auth/forgot-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + STAFF_EMAIL + "\"}"))
        .andExpect(status().isNotImplemented())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
        .andExpect(jsonPath("$.status").value(501))
        .andExpect(jsonPath("$.detail").value("This feature is not yet implemented"))
        .andExpect(jsonPath("$.instance").value("/auth/forgot-password"));
  }

  @Test
  @DisplayName(
      "POST /auth/forgot-password - Given empty body, when forgot password, then returns"
          + " validation errors")
  void givenEmptyBody_whenForgotPassword_thenReturnsUnprocessableEntity() throws Exception {
    mockMvc
        .perform(
            post("/auth/forgot-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
        .andExpect(jsonPath("$.errors.email").isNotEmpty());
  }

  @Test
  @DisplayName(
      "POST /auth/reset-password - Given valid body, when reset password, then returns not"
          + " implemented")
  void givenValidBody_whenResetPassword_thenReturnsNotImplemented() throws Exception {
    mockMvc
        .perform(
            post("/auth/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"password\": \"newsecret123\", \"confirm_password\": \"newsecret123\"}"))
        .andExpect(status().isNotImplemented())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
        .andExpect(jsonPath("$.status").value(501))
        .andExpect(jsonPath("$.detail").value("This feature is not yet implemented"))
        .andExpect(jsonPath("$.instance").value("/auth/reset-password"));
  }

  @Test
  @DisplayName(
      "POST /auth/reset-password - Given empty body, when reset password, then returns"
          + " validation errors")
  void givenEmptyBody_whenResetPassword_thenReturnsUnprocessableEntity() throws Exception {
    mockMvc
        .perform(
            post("/auth/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
        .andExpect(jsonPath("$.errors.password").isNotEmpty())
        .andExpect(jsonPath("$.errors.confirmPassword").isNotEmpty());
  }
}
