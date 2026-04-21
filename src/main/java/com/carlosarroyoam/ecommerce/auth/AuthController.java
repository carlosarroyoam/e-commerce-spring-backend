package com.carlosarroyoam.ecommerce.auth;

import com.carlosarroyoam.ecommerce.auth.dto.ForgotPasswordRequest;
import com.carlosarroyoam.ecommerce.auth.dto.LoginRequest;
import com.carlosarroyoam.ecommerce.auth.dto.LoginResponse;
import com.carlosarroyoam.ecommerce.auth.dto.RefreshTokenResponse;
import com.carlosarroyoam.ecommerce.auth.dto.ResetPasswordRequest;
import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.property.JwtProps;
import com.carlosarroyoam.ecommerce.core.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
  private final AuthService authService;
  private final CookieUtil cookieUtil;
  private final JwtProps jwtProps;

  public AuthController(AuthService authService, CookieUtil cookieUtil, JwtProps jwtProps) {
    this.authService = authService;
    this.cookieUtil = cookieUtil;
    this.jwtProps = jwtProps;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
      HttpServletResponse response) {
    LoginResponse loginResponse = authService.login(request);

    response.addHeader(HttpHeaders.SET_COOKIE,
        cookieUtil
            .createCookie(REFRESH_TOKEN_COOKIE_NAME, loginResponse.getRefreshToken(),
                Duration.ofMillis(jwtProps.getRefreshTokenTtlMs()))
            .toString());

    return ResponseEntity.ok(loginResponse);
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<RefreshTokenResponse> refreshToken(
      @CookieValue(name = "refresh_token", required = false) String refreshTokenCookie,
      HttpServletResponse response) {

    if (refreshTokenCookie == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
          AppMessages.JWT_REFRESH_TOKEN_IS_REQUIRED);
    }

    RefreshTokenResponse refreshTokenResponse = authService.refreshToken(refreshTokenCookie);

    response.addHeader(HttpHeaders.SET_COOKIE,
        cookieUtil
            .createCookie(REFRESH_TOKEN_COOKIE_NAME, refreshTokenResponse.getRefreshToken(),
                Duration.ofMillis(jwtProps.getRefreshTokenTtlMs()))
            .toString());

    return ResponseEntity.ok(refreshTokenResponse);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @CookieValue(name = "refresh_token", required = false) String refreshTokenCookie,
      HttpServletResponse response) {
    authService.revoke(refreshTokenCookie);

    response.addHeader(HttpHeaders.SET_COOKIE,
        cookieUtil.deleteCookie(REFRESH_TOKEN_COOKIE_NAME).toString());

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    authService.forgotPassword(request);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/reset-password")
  public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request);
    return ResponseEntity.noContent().build();
  }
}
