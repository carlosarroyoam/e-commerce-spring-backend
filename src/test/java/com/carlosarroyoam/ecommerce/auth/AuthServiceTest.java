package com.carlosarroyoam.ecommerce.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.ecommerce.auth.dto.AuthResponse;
import com.carlosarroyoam.ecommerce.auth.dto.ForgotPasswordRequest;
import com.carlosarroyoam.ecommerce.auth.dto.LoginRequest;
import com.carlosarroyoam.ecommerce.auth.dto.ResetPasswordRequest;
import com.carlosarroyoam.ecommerce.auth.entity.RefreshToken;
import com.carlosarroyoam.ecommerce.auth.principal.AuthPrincipal;
import com.carlosarroyoam.ecommerce.auth.principal.PrincipalType;
import com.carlosarroyoam.ecommerce.core.constant.MessageKeys;
import com.carlosarroyoam.ecommerce.core.exception.UnauthorizedException;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
  private static final String DEVICE_ID = "device-1";
  private static final String RAW_REFRESH_TOKEN = "raw-refresh-token";
  private static final String NEW_RAW_REFRESH_TOKEN = "new-raw-refresh-token";

  @Mock private AuthenticationManager authenticationManager;
  @Mock private StaffDetailsService staffDetailsService;
  @Mock private CustomerDetailsService customerDetailsService;
  @Mock private RefreshTokenService refreshTokenService;
  @Mock private TokenService tokenService;
  @InjectMocks private AuthService authService;

  private AuthPrincipal staffPrincipal;
  private AuthPrincipal customerPrincipal;
  private UUID refreshTokenId;
  private String refreshTokenCookie;

  @BeforeEach
  void setUp() {
    staffPrincipal =
        AuthPrincipal.builder()
            .id(1L)
            .fullName("Carlos Alberto Arroyo Martínez")
            .firstName("Carlos Alberto")
            .lastName("Arroyo Martínez")
            .email("carlos.arroyo@e-commerce.com")
            .passwordHash("hash")
            .status("ACTIVE")
            .principalType(PrincipalType.STAFF)
            .roles(Set.of("USER", "ADMIN"))
            .build();

    customerPrincipal =
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

    refreshTokenId = UUID.fromString("11111111-2222-3333-4444-555555555555");
    refreshTokenCookie = refreshTokenId + "." + RAW_REFRESH_TOKEN;
  }

  private LoginRequest loginRequest() {
    return LoginRequest.builder()
        .email("carlos.arroyo@e-commerce.com")
        .password("secret123")
        .deviceId(DEVICE_ID)
        .build();
  }

  private RefreshToken storedRefreshToken(PrincipalType principalType, Long principalId) {
    return RefreshToken.builder()
        .id(refreshTokenId)
        .principalType(principalType)
        .principalId(principalId)
        .deviceId(DEVICE_ID)
        .build();
  }

  @Test
  @DisplayName(
      "Given valid credentials, when login, then returns auth response with sorted roles and"
          + " composite refresh token")
  void givenValidCredentials_whenLogin_thenReturnsAuthResponse() {
    when(authenticationManager.authenticate(any()))
        .thenReturn(
            UsernamePasswordAuthenticationToken.authenticated(
                staffPrincipal, null, staffPrincipal.getAuthorities()));
    when(tokenService.generateAccessToken(staffPrincipal)).thenReturn("access-token");
    when(tokenService.generateRefreshToken()).thenReturn(RAW_REFRESH_TOKEN);
    when(refreshTokenService.save(staffPrincipal, DEVICE_ID, RAW_REFRESH_TOKEN))
        .thenReturn(storedRefreshToken(PrincipalType.STAFF, 1L));

    AuthResponse response = authService.login(loginRequest());

    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getFullName()).isEqualTo("Carlos Alberto Arroyo Martínez");
    assertThat(response.getFirstName()).isEqualTo("Carlos Alberto");
    assertThat(response.getLastName()).isEqualTo("Arroyo Martínez");
    assertThat(response.getEmail()).isEqualTo("carlos.arroyo@e-commerce.com");
    assertThat(response.getRoles()).containsExactly("ADMIN", "USER");
    assertThat(response.getAccessToken()).isEqualTo("access-token");
    assertThat(response.getRefreshToken()).isEqualTo(refreshTokenCookie);
    verify(tokenService).generateAccessToken(staffPrincipal);
    verify(tokenService).generateRefreshToken();
    verify(refreshTokenService).save(staffPrincipal, DEVICE_ID, RAW_REFRESH_TOKEN);
  }

  @Test
  @DisplayName("Given invalid credentials, when login, then propagates the authentication error")
  void givenInvalidCredentials_whenLogin_thenThrowsBadCredentialsException() {
    when(authenticationManager.authenticate(any()))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    assertThatThrownBy(() -> authService.login(loginRequest()))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Bad credentials");

    verifyNoInteractions(tokenService);
    verify(refreshTokenService, never()).save(any(), anyString(), anyString());
  }

  @Test
  @DisplayName("Given null cookie, when refresh token, then throws required exception")
  void givenNullCookie_whenRefreshToken_thenThrowsUnauthorizedException() {
    assertThatThrownBy(() -> authService.refreshToken(null))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage(MessageKeys.REFRESH_TOKEN_IS_REQUIRED_EXCEPTION);

    verifyNoInteractions(refreshTokenService);
  }

  @Test
  @DisplayName("Given blank cookie, when refresh token, then throws required exception")
  void givenBlankCookie_whenRefreshToken_thenThrowsUnauthorizedException() {
    assertThatThrownBy(() -> authService.refreshToken("  "))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage(MessageKeys.REFRESH_TOKEN_IS_REQUIRED_EXCEPTION);

    verifyNoInteractions(refreshTokenService);
  }

  @Test
  @DisplayName("Given cookie without dot, when refresh token, then throws invalid exception")
  void givenCookieWithoutDot_whenRefreshToken_thenThrowsUnauthorizedException() {
    assertThatThrownBy(() -> authService.refreshToken("not-a-composite-token"))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage(MessageKeys.REFRESH_TOKEN_IS_NOT_VALID_EXCEPTION);

    verifyNoInteractions(refreshTokenService);
  }

  @Test
  @DisplayName("Given cookie with invalid uuid, when refresh token, then throws invalid exception")
  void givenCookieWithInvalidUuid_whenRefreshToken_thenThrowsUnauthorizedException() {
    assertThatThrownBy(() -> authService.refreshToken("not-a-uuid." + RAW_REFRESH_TOKEN))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage(MessageKeys.REFRESH_TOKEN_IS_NOT_VALID_EXCEPTION);

    verifyNoInteractions(refreshTokenService);
  }

  @Test
  @DisplayName(
      "Given valid staff cookie, when refresh token, then loads staff principal and rotates token")
  void givenValidStaffCookie_whenRefreshToken_thenReturnsAuthResponse() {
    when(refreshTokenService.findById(refreshTokenId))
        .thenReturn(storedRefreshToken(PrincipalType.STAFF, 1L));
    when(staffDetailsService.loadUserById(1L)).thenReturn(staffPrincipal);
    when(tokenService.generateAccessToken(staffPrincipal)).thenReturn("new-access-token");
    when(tokenService.generateRefreshToken()).thenReturn(NEW_RAW_REFRESH_TOKEN);
    when(refreshTokenService.rotate(refreshTokenId, RAW_REFRESH_TOKEN, NEW_RAW_REFRESH_TOKEN))
        .thenReturn(storedRefreshToken(PrincipalType.STAFF, 1L));

    AuthResponse response = authService.refreshToken(refreshTokenCookie);

    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getRoles()).containsExactly("ADMIN", "USER");
    assertThat(response.getAccessToken()).isEqualTo("new-access-token");
    assertThat(response.getRefreshToken()).isEqualTo(refreshTokenId + "." + NEW_RAW_REFRESH_TOKEN);
    verify(refreshTokenService).rotate(refreshTokenId, RAW_REFRESH_TOKEN, NEW_RAW_REFRESH_TOKEN);
    verifyNoInteractions(customerDetailsService);
  }

  @Test
  @DisplayName(
      "Given valid customer cookie, when refresh token, then loads customer principal and rotates"
          + " token")
  void givenValidCustomerCookie_whenRefreshToken_thenReturnsAuthResponse() {
    when(refreshTokenService.findById(refreshTokenId))
        .thenReturn(storedRefreshToken(PrincipalType.CUSTOMER, 7L));
    when(customerDetailsService.loadUserById(7L)).thenReturn(customerPrincipal);
    when(tokenService.generateAccessToken(customerPrincipal)).thenReturn("new-access-token");
    when(tokenService.generateRefreshToken()).thenReturn(NEW_RAW_REFRESH_TOKEN);
    when(refreshTokenService.rotate(refreshTokenId, RAW_REFRESH_TOKEN, NEW_RAW_REFRESH_TOKEN))
        .thenReturn(storedRefreshToken(PrincipalType.CUSTOMER, 7L));

    AuthResponse response = authService.refreshToken(refreshTokenCookie);

    assertThat(response.getId()).isEqualTo(7L);
    assertThat(response.getRoles()).containsExactly("CUSTOMER");
    assertThat(response.getAccessToken()).isEqualTo("new-access-token");
    assertThat(response.getRefreshToken()).isEqualTo(refreshTokenId + "." + NEW_RAW_REFRESH_TOKEN);
    verify(refreshTokenService).rotate(refreshTokenId, RAW_REFRESH_TOKEN, NEW_RAW_REFRESH_TOKEN);
    verifyNoInteractions(staffDetailsService);
  }

  @Test
  @DisplayName(
      "Given principal no longer exists, when refresh token, then throws invalid exception")
  void givenNonExistentPrincipal_whenRefreshToken_thenThrowsUnauthorizedException() {
    when(refreshTokenService.findById(refreshTokenId))
        .thenReturn(storedRefreshToken(PrincipalType.STAFF, 99L));
    when(staffDetailsService.loadUserById(99L))
        .thenThrow(new UsernameNotFoundException("User not found: 99"));

    assertThatThrownBy(() -> authService.refreshToken(refreshTokenCookie))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage(MessageKeys.REFRESH_TOKEN_IS_NOT_VALID_EXCEPTION);

    verifyNoInteractions(tokenService);
    verify(refreshTokenService, never()).rotate(any(), anyString(), anyString());
  }

  @Test
  @DisplayName("Given unknown refresh token, when refresh token, then propagates invalid exception")
  void givenUnknownRefreshToken_whenRefreshToken_thenThrowsUnauthorizedException() {
    when(refreshTokenService.findById(refreshTokenId))
        .thenThrow(new UnauthorizedException(MessageKeys.REFRESH_TOKEN_IS_NOT_VALID_EXCEPTION));

    assertThatThrownBy(() -> authService.refreshToken(refreshTokenCookie))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage(MessageKeys.REFRESH_TOKEN_IS_NOT_VALID_EXCEPTION);

    verifyNoInteractions(staffDetailsService, customerDetailsService, tokenService);
  }

  @Test
  @DisplayName("Given valid cookie, when revoke, then revokes the refresh token")
  void givenValidCookie_whenRevoke_thenRevokesRefreshToken() {
    authService.revoke(refreshTokenCookie);

    verify(refreshTokenService).revoke(refreshTokenId, RAW_REFRESH_TOKEN);
  }

  @Test
  @DisplayName("Given null cookie, when revoke, then does nothing")
  void givenNullCookie_whenRevoke_thenDoesNothing() {
    authService.revoke(null);

    verify(refreshTokenService, never()).revoke(any(), anyString());
  }

  @Test
  @DisplayName("Given invalid cookie, when revoke, then does nothing")
  void givenInvalidCookie_whenRevoke_thenDoesNothing() {
    authService.revoke("not-a-uuid.token");
    authService.revoke("no-dot");

    verify(refreshTokenService, never()).revoke(any(), anyString());
  }

  @Test
  @DisplayName("Given any request, when forgot password, then throws not implemented")
  void givenAnyRequest_whenForgotPassword_thenThrowsNotImplemented() {
    ForgotPasswordRequest request =
        ForgotPasswordRequest.builder().email("carlos.arroyo@e-commerce.com").build();

    assertThatThrownBy(() -> authService.forgotPassword(request))
        .isInstanceOfSatisfying(
            ResponseStatusException.class,
            ex -> {
              assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
              assertThat(ex.getReason()).isEqualTo(MessageKeys.FEATURE_NOT_IMPLEMENTED_EXCEPTION);
            });
  }

  @Test
  @DisplayName("Given any request, when reset password, then throws not implemented")
  void givenAnyRequest_whenResetPassword_thenThrowsNotImplemented() {
    ResetPasswordRequest request =
        ResetPasswordRequest.builder().password("secret123").confirmPassword("secret123").build();

    assertThatThrownBy(() -> authService.resetPassword(request))
        .isInstanceOfSatisfying(
            ResponseStatusException.class,
            ex -> {
              assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
              assertThat(ex.getReason()).isEqualTo(MessageKeys.FEATURE_NOT_IMPLEMENTED_EXCEPTION);
            });
  }
}
