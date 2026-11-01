package com.carlosarroyoam.ecommerce.auth;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.ecommerce.auth.dto.AuthResponse;
import com.carlosarroyoam.ecommerce.auth.dto.ForgotPasswordRequest;
import com.carlosarroyoam.ecommerce.auth.dto.LoginRequest;
import com.carlosarroyoam.ecommerce.auth.dto.ResetPasswordRequest;
import com.carlosarroyoam.ecommerce.core.constant.MessageKeys;
import com.carlosarroyoam.ecommerce.core.exception.ExceptionLogger;
import com.carlosarroyoam.ecommerce.core.exception.GlobalExceptionHandler;
import com.carlosarroyoam.ecommerce.core.exception.ProblemDetailFactory;
import com.carlosarroyoam.ecommerce.core.exception.UnauthorizedException;
import com.carlosarroyoam.ecommerce.core.property.CookieProps;
import com.carlosarroyoam.ecommerce.core.property.JwtProps;
import com.carlosarroyoam.ecommerce.core.util.CookieUtils;
import com.carlosarroyoam.ecommerce.support.testutils.TestMessageSources;
import com.carlosarroyoam.ecommerce.support.testutils.TestObjectMappers;
import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
  private static final String REFRESH_TOKEN_ID = "11111111-2222-3333-4444-555555555555";
  private static final String COMPOSITE_REFRESH_TOKEN = REFRESH_TOKEN_ID + ".raw-token";
  private static final String LOGIN_BODY =
      """
      {"email": "carlos.arroyo@e-commerce.com", "password": "secret123", "device_id": "device-1"}
      """;

  @Mock private AuthService authService;

  private MockMvc mockMvc;
  private AuthResponse authResponse;

  @BeforeEach
  void setup() {
    CookieProps cookieProps = new CookieProps();
    cookieProps.setSecure(false);

    JwtProps jwtProps = new JwtProps();
    jwtProps.setAccessTokenTtlMs(300_000L);
    jwtProps.setRefreshTokenTtlMs(86_400_000L);
    jwtProps.setRefreshTokenMaxLifetimeMs(2_592_000_000L);

    authResponse =
        AuthResponse.builder()
            .id(1L)
            .fullName("Carlos Alberto Arroyo Martínez")
            .firstName("Carlos Alberto")
            .lastName("Arroyo Martínez")
            .email("carlos.arroyo@e-commerce.com")
            .roles(List.of("ADMIN"))
            .accessToken("access-token")
            .refreshToken(COMPOSITE_REFRESH_TOKEN)
            .build();

    AuthController authController =
        new AuthController(authService, new CookieUtils(cookieProps), jwtProps);

    mockMvc =
        MockMvcBuilders.standaloneSetup(authController)
            .setControllerAdvice(
                new GlobalExceptionHandler(
                    new ProblemDetailFactory(),
                    new ExceptionLogger(),
                    TestMessageSources.messages()))
            .setMessageConverters(
                new MappingJackson2HttpMessageConverter(TestObjectMappers.snakeCase()))
            .build();
  }

  @Test
  @DisplayName(
      "POST /auth/login - Given valid credentials, when login, then returns auth response and sets"
          + " refresh token cookie")
  void givenValidCredentials_whenLogin_thenReturnsAuthResponseAndSetsCookie() throws Exception {
    when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

    mockMvc
        .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.full_name").value("Carlos Alberto Arroyo Martínez"))
        .andExpect(jsonPath("$.first_name").value("Carlos Alberto"))
        .andExpect(jsonPath("$.last_name").value("Arroyo Martínez"))
        .andExpect(jsonPath("$.email").value("carlos.arroyo@e-commerce.com"))
        .andExpect(jsonPath("$.roles.length()").value(1))
        .andExpect(jsonPath("$.roles[0]").value("ADMIN"))
        .andExpect(jsonPath("$.access_token").value("access-token"))
        .andExpect(jsonPath("$.refresh_token").doesNotExist())
        .andExpect(
            header()
                .string(
                    HttpHeaders.SET_COOKIE,
                    containsString("refresh_token=" + COMPOSITE_REFRESH_TOKEN)))
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=86400")));

    verify(authService)
        .login(
            argThat(request ->
                "carlos.arroyo@e-commerce.com".equals(request.getEmail())
                    && "secret123".equals(request.getPassword())
                    && "device-1".equals(request.getDeviceId())));
  }

  @Test
  @DisplayName("POST /auth/login - Given empty body, when login, then returns validation errors")
  void givenEmptyBody_whenLogin_thenReturnsUnprocessableEntity() throws Exception {
    mockMvc
        .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.detail").value("Invalid request data"))
        .andExpect(jsonPath("$.instance").value("/auth/login"))
        .andExpect(jsonPath("$.errors.email").value("Email must not be blank"))
        .andExpect(jsonPath("$.errors.password").value("Password must not be blank"))
        .andExpect(jsonPath("$.errors.deviceId").value("Device id must not be blank"));

    verifyNoInteractions(authService);
  }

  @Test
  @DisplayName(
      "POST /auth/refresh-token - Given refresh token cookie, when refresh token, then returns"
          + " auth response and sets a new cookie")
  void givenRefreshTokenCookie_whenRefreshToken_thenReturnsAuthResponseAndSetsCookie()
      throws Exception {
    String newCompositeToken = REFRESH_TOKEN_ID + ".new-raw-token";
    authResponse.setRefreshToken(newCompositeToken);
    when(authService.refreshToken(COMPOSITE_REFRESH_TOKEN)).thenReturn(authResponse);

    mockMvc
        .perform(
            post("/auth/refresh-token")
                .cookie(new Cookie("refresh_token", COMPOSITE_REFRESH_TOKEN)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.access_token").value("access-token"))
        .andExpect(jsonPath("$.refresh_token").doesNotExist())
        .andExpect(
            header()
                .string(
                    HttpHeaders.SET_COOKIE, containsString("refresh_token=" + newCompositeToken)))
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=86400")));

    verify(authService).refreshToken(COMPOSITE_REFRESH_TOKEN);
  }

  @Test
  @DisplayName(
      "POST /auth/refresh-token - Given no cookie, when refresh token, then returns unauthorized")
  void givenNoCookie_whenRefreshToken_thenReturnsUnauthorized() throws Exception {
    when(authService.refreshToken(null))
        .thenThrow(new UnauthorizedException(MessageKeys.REFRESH_TOKEN_IS_REQUIRED_EXCEPTION));

    mockMvc
        .perform(post("/auth/refresh-token"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.detail").value("The refresh token is required"))
        .andExpect(jsonPath("$.instance").value("/auth/refresh-token"));
  }

  @Test
  @DisplayName(
      "POST /auth/logout - Given refresh token cookie, when logout, then revokes token and clears"
          + " cookie")
  void givenRefreshTokenCookie_whenLogout_thenRevokesTokenAndClearsCookie() throws Exception {
    mockMvc
        .perform(post("/auth/logout").cookie(new Cookie("refresh_token", COMPOSITE_REFRESH_TOKEN)))
        .andExpect(status().isNoContent())
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refresh_token=;")))
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")))
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")));

    verify(authService).revoke(COMPOSITE_REFRESH_TOKEN);
  }

  @Test
  @DisplayName(
      "POST /auth/forgot-password - Given valid body, when forgot password, then returns"
          + " not implemented")
  void givenValidBody_whenForgotPassword_thenReturnsNotImplemented() throws Exception {
    doThrow(
            new ResponseStatusException(
                HttpStatus.NOT_IMPLEMENTED, MessageKeys.FEATURE_NOT_IMPLEMENTED_EXCEPTION))
        .when(authService)
        .forgotPassword(any(ForgotPasswordRequest.class));

    mockMvc
        .perform(
            post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"carlos.arroyo@e-commerce.com\"}"))
        .andExpect(status().isNotImplemented())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(501))
        .andExpect(jsonPath("$.title").value("Not Implemented"))
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
            post("/auth/forgot-password").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.errors.email").value("Email must not be blank"));

    verifyNoInteractions(authService);
  }

  @Test
  @DisplayName(
      "POST /auth/reset-password - Given valid body, when reset password, then returns"
          + " not implemented")
  void givenValidBody_whenResetPassword_thenReturnsNotImplemented() throws Exception {
    doThrow(
            new ResponseStatusException(
                HttpStatus.NOT_IMPLEMENTED, MessageKeys.FEATURE_NOT_IMPLEMENTED_EXCEPTION))
        .when(authService)
        .resetPassword(any(ResetPasswordRequest.class));

    mockMvc
        .perform(
            post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\": \"secret123\", \"confirm_password\": \"secret123\"}"))
        .andExpect(status().isNotImplemented())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
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
        .perform(post("/auth/reset-password").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.errors.password").value("Password must not be blank"))
        .andExpect(
            jsonPath("$.errors.confirmPassword").value("Confirm password must not be blank"));

    verifyNoInteractions(authService);
  }
}
