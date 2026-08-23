package com.carlosarroyoam.ecommerce.auth;

import com.carlosarroyoam.ecommerce.auth.dto.AuthResponse;
import com.carlosarroyoam.ecommerce.auth.dto.ForgotPasswordRequest;
import com.carlosarroyoam.ecommerce.auth.dto.LoginRequest;
import com.carlosarroyoam.ecommerce.auth.dto.ResetPasswordRequest;
import com.carlosarroyoam.ecommerce.auth.entity.RefreshToken;
import com.carlosarroyoam.ecommerce.auth.principal.AuthPrincipal;
import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

/**
 * Orquesta el flujo de autenticación: autentica credenciales, emite y rota tokens, revoca sesiones,
 * y expone (sin implementar todavía) la recuperación y reseteo de contraseña.
 */
@Service
public class AuthService {
  private static final Logger log = LoggerFactory.getLogger(AuthService.class);
  private final AuthenticationManager authenticationManager;
  private final StaffDetailsService staffDetailsService;
  private final CustomerDetailsService customerDetailsService;
  private final RefreshTokenService refreshTokenService;
  private final TokenService tokenService;

  public AuthService(
      AuthenticationManager authenticationManager,
      StaffDetailsService staffDetailsService,
      CustomerDetailsService customerDetailsService,
      RefreshTokenService refreshTokenService,
      TokenService tokenService) {
    this.authenticationManager = authenticationManager;
    this.staffDetailsService = staffDetailsService;
    this.customerDetailsService = customerDetailsService;
    this.refreshTokenService = refreshTokenService;
    this.tokenService = tokenService;
  }

  /**
   * Autentica al usuario (staff o customer) con email y contraseña, y crea un nuevo par de
   * access/refresh token asociado al dispositivo indicado.
   *
   * @param request credenciales de acceso y el identificador del dispositivo
   * @return la respuesta de autenticación con el access token y el refresh token generados
   * @throws org.springframework.security.core.AuthenticationException si las credenciales no son
   *     válidas
   */
  public AuthResponse login(LoginRequest request) {
    Authentication auth =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    AuthPrincipal principal = (AuthPrincipal) auth.getPrincipal();

    String accessToken = tokenService.generateAccessToken(principal);
    String refreshToken = tokenService.generateRefreshToken();

    RefreshToken createdRefreshToken =
        refreshTokenService.save(principal, request.getDeviceId(), refreshToken);

    return buildAuthResponse(principal, accessToken, createdRefreshToken, refreshToken);
  }

  /**
   * Valida el refresh token recibido, carga al principal asociado, y rota tanto el access token
   * como el refresh token.
   *
   * @param rawRefreshTokenCookie valor crudo de la cookie {@code refresh_token} (formato {@code
   *     id.rawToken}), puede ser nulo o vacío
   * @return la respuesta de autenticación con el access token y el refresh token renovados
   * @throws org.springframework.web.server.ResponseStatusException con 401 Unauthorized si el token
   *     no está presente, tiene formato inválido, no existe o pertenece a un principal que ya no
   *     existe
   */
  public AuthResponse refreshToken(String rawRefreshTokenCookie) {
    if (!StringUtils.hasText(rawRefreshTokenCookie)) {
      log.warn(AppMessages.JWT_REFRESH_TOKEN_IS_REQUIRED);
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, AppMessages.JWT_REFRESH_TOKEN_IS_REQUIRED);
    }

    RefreshTokenCookie refreshTokenCookie =
        parseRefreshTokenCookie(rawRefreshTokenCookie)
            .orElseThrow(
                () -> {
                  log.warn(AppMessages.JWT_REFRESH_TOKEN_IS_NOT_VALID);
                  return new ResponseStatusException(
                      HttpStatus.UNAUTHORIZED, AppMessages.JWT_REFRESH_TOKEN_IS_NOT_VALID);
                });

    RefreshToken refreshTokenById = refreshTokenService.findById(refreshTokenCookie.getId());
    AuthPrincipal principal = loadPrincipal(refreshTokenById);

    String accessToken = tokenService.generateAccessToken(principal);
    String refreshToken = tokenService.generateRefreshToken();

    RefreshToken createdRefreshToken =
        refreshTokenService.rotate(
            refreshTokenCookie.getId(), refreshTokenCookie.getRawToken(), refreshToken);

    return buildAuthResponse(principal, accessToken, createdRefreshToken, refreshToken);
  }

  /**
   * Revoca (elimina) el refresh token asociado a la cookie recibida, si es válida. Si la cookie es
   * nula, vacía o tiene formato inválido, no hace nada.
   *
   * @param rawRefreshTokenCookie valor crudo de la cookie {@code refresh_token} (formato {@code
   *     id.rawToken}), puede ser nulo o vacío
   */
  public void revoke(String rawRefreshTokenCookie) {
    parseRefreshTokenCookie(rawRefreshTokenCookie)
        .ifPresent(
            refreshTokenCookie ->
                refreshTokenService.revoke(
                    refreshTokenCookie.getId(), refreshTokenCookie.getRawToken()));
  }

  /**
   * Funcionalidad aún no implementada. Siempre lanza 501 Not Implemented.
   *
   * @param request email del usuario que solicita el reseteo
   * @throws org.springframework.web.server.ResponseStatusException con 501 Not Implemented
   */
  public void forgotPassword(ForgotPasswordRequest request) {
    throw new ResponseStatusException(
        HttpStatus.NOT_IMPLEMENTED, AppMessages.FEATURE_NOT_IMPLEMENTED_EXCEPTION);
  }

  /**
   * Funcionalidad aún no implementada. Siempre lanza 501 Not Implemented.
   *
   * @param request nueva contraseña y su confirmación
   * @throws org.springframework.web.server.ResponseStatusException con 501 Not Implemented
   */
  public void resetPassword(ResetPasswordRequest request) {
    throw new ResponseStatusException(
        HttpStatus.NOT_IMPLEMENTED, AppMessages.FEATURE_NOT_IMPLEMENTED_EXCEPTION);
  }

  /**
   * Resuelve el {@link AuthPrincipal} (staff o customer) dueño del refresh token, según su tipo de
   * principal.
   *
   * @param refreshTokenById el refresh token del cual obtener el principal
   * @return el principal autenticado correspondiente
   * @throws org.springframework.web.server.ResponseStatusException con 401 Unauthorized si el
   *     principal ya no existe
   */
  private AuthPrincipal loadPrincipal(RefreshToken refreshTokenById) {
    try {
      return switch (refreshTokenById.getPrincipalType()) {
        case STAFF ->
            (AuthPrincipal) staffDetailsService.loadUserById(refreshTokenById.getPrincipalId());
        case CUSTOMER ->
            (AuthPrincipal) customerDetailsService.loadUserById(refreshTokenById.getPrincipalId());
      };
    } catch (UsernameNotFoundException ex) {
      log.warn(AppMessages.JWT_REFRESH_TOKEN_IS_NOT_VALID);
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, AppMessages.JWT_REFRESH_TOKEN_IS_NOT_VALID);
    }
  }

  /**
   * Parsea el valor crudo de la cookie {@code refresh_token} (formato {@code id.rawToken}) en un
   * {@link RefreshTokenCookie}.
   *
   * @param rawRefreshTokenCookie valor crudo de la cookie, puede ser nulo o vacío
   * @return el refresh token parseado, o {@link Optional#empty()} si es nulo, vacío, no tiene el
   *     formato esperado, o el id no es un UUID válido
   */
  private Optional<RefreshTokenCookie> parseRefreshTokenCookie(String rawRefreshTokenCookie) {
    if (!StringUtils.hasText(rawRefreshTokenCookie)) {
      return Optional.empty();
    }

    String[] refreshTokenParts = rawRefreshTokenCookie.split("\\.", 2);
    if (refreshTokenParts.length != 2) {
      return Optional.empty();
    }

    try {
      return Optional.of(
          RefreshTokenCookie.builder()
              .id(UUID.fromString(refreshTokenParts[0]))
              .rawToken(refreshTokenParts[1])
              .build());
    } catch (IllegalArgumentException ex) {
      return Optional.empty();
    }
  }

  /**
   * Construye el DTO de respuesta de autenticación a partir del principal y los tokens recién
   * generados.
   *
   * @param principal el principal autenticado
   * @param accessToken el access token JWT generado
   * @param createdRefreshToken la entidad de refresh token persistida
   * @param refreshToken el valor sin hashear del refresh token generado
   * @return el DTO {@link AuthResponse} listo para devolver al cliente
   */
  private AuthResponse buildAuthResponse(
      AuthPrincipal principal,
      String accessToken,
      RefreshToken createdRefreshToken,
      String refreshToken) {
    return AuthResponse.builder()
        .id(principal.getId())
        .fullName(principal.getFullName())
        .firstName(principal.getFirstName())
        .lastName(principal.getLastName())
        .email(principal.getEmail())
        .roles(principal.getRoles().stream().sorted().toList())
        .accessToken(accessToken)
        .refreshToken(createdRefreshToken.getId() + "." + refreshToken)
        .build();
  }
}
