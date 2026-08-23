package com.carlosarroyoam.ecommerce.auth;

import com.carlosarroyoam.ecommerce.auth.dto.AuthResponse;
import com.carlosarroyoam.ecommerce.auth.dto.ForgotPasswordRequest;
import com.carlosarroyoam.ecommerce.auth.dto.LoginRequest;
import com.carlosarroyoam.ecommerce.auth.dto.ResetPasswordRequest;
import com.carlosarroyoam.ecommerce.core.property.JwtProps;
import com.carlosarroyoam.ecommerce.core.util.CookieUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone los endpoints REST del flujo de autenticación bajo {@code /auth}: login, renovación de
 * token, logout, y las operaciones (aún no implementadas) de recuperación y reseteo de
 * contraseña.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
  private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
  private final AuthService authService;
  private final CookieUtils cookieUtils;
  private final JwtProps jwtProps;

  public AuthController(AuthService authService, CookieUtils cookieUtils, JwtProps jwtProps) {
    this.authService = authService;
    this.cookieUtils = cookieUtils;
    this.jwtProps = jwtProps;
  }

  /**
   * Autentica al usuario con email y contraseña y establece la cookie {@code refresh_token} en
   * la respuesta.
   *
   * @param request credenciales de acceso y el identificador del dispositivo
   * @param response respuesta HTTP donde se añade la cookie de refresh token
   * @return el access token, los datos del principal y el estado 200 OK
   */
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(
      @Valid @RequestBody LoginRequest request, HttpServletResponse response) {
    AuthResponse loginResponse = authService.login(request);

    ResponseCookie refreshTokenCookie =
        cookieUtils.createCookie(
            REFRESH_TOKEN_COOKIE_NAME,
            loginResponse.getRefreshToken(),
            Duration.ofMillis(jwtProps.getRefreshTokenTtlMs()));

    response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    return ResponseEntity.ok(loginResponse);
  }

  /**
   * Rota el refresh token recibido en la cookie {@code refresh_token} y emite un nuevo access
   * token junto con un nuevo refresh token en la respuesta.
   *
   * @param rawRefreshTokenCookie valor crudo de la cookie {@code refresh_token}, puede ser nulo
   * @param response respuesta HTTP donde se añade la cookie con el refresh token renovado
   * @return el nuevo access token, los datos del principal y el estado 200 OK
   */
  @PostMapping("/refresh-token")
  public ResponseEntity<AuthResponse> refreshToken(
      @CookieValue(name = "refresh_token", required = false) String rawRefreshTokenCookie,
      HttpServletResponse response) {
    AuthResponse refreshTokenResponse = authService.refreshToken(rawRefreshTokenCookie);

    ResponseCookie refreshTokenCookie =
        cookieUtils.createCookie(
            REFRESH_TOKEN_COOKIE_NAME,
            refreshTokenResponse.getRefreshToken(),
            Duration.ofMillis(jwtProps.getRefreshTokenTtlMs()));

    response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    return ResponseEntity.ok(refreshTokenResponse);
  }

  /**
   * Revoca el refresh token asociado a la cookie {@code refresh_token} y elimina dicha cookie del
   * navegador.
   *
   * @param rawRefreshTokenCookie valor crudo de la cookie {@code refresh_token}, puede ser nulo
   * @param response respuesta HTTP donde se añade la cabecera que elimina la cookie
   * @return estado 204 No Content
   */
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @CookieValue(name = "refresh_token", required = false) String rawRefreshTokenCookie,
      HttpServletResponse response) {
    authService.revoke(rawRefreshTokenCookie);

    ResponseCookie deleteCookie = cookieUtils.deleteCookie(REFRESH_TOKEN_COOKIE_NAME);

    response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    return ResponseEntity.noContent().build();
  }

  /**
   * Inicia el flujo de recuperación de contraseña para el email indicado.
   *
   * <p>Funcionalidad aún no implementada: siempre responde con 501 Not Implemented (ver {@link
   * AuthService#forgotPassword(ForgotPasswordRequest)}).
   *
   * @param request email del usuario que solicita el reseteo
   * @return nunca retorna normalmente, lanza {@link
   *     org.springframework.web.server.ResponseStatusException}
   */
  @PostMapping("/forgot-password")
  public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    authService.forgotPassword(request);
    return ResponseEntity.noContent().build();
  }

  /**
   * Completa el flujo de reseteo de contraseña con la nueva contraseña provista.
   *
   * <p>Funcionalidad aún no implementada: siempre responde con 501 Not Implemented (ver {@link
   * AuthService#resetPassword(ResetPasswordRequest)}).
   *
   * @param request nueva contraseña y su confirmación
   * @return nunca retorna normalmente, lanza {@link
   *     org.springframework.web.server.ResponseStatusException}
   */
  @PostMapping("/reset-password")
  public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request);
    return ResponseEntity.noContent().build();
  }
}
