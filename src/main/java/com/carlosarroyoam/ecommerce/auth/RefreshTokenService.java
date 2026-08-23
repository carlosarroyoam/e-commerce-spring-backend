package com.carlosarroyoam.ecommerce.auth;

import com.carlosarroyoam.ecommerce.auth.entity.RefreshToken;
import com.carlosarroyoam.ecommerce.auth.principal.AuthPrincipal;
import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.property.JwtProps;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Persiste, rota y revoca los refresh tokens, validando su expiración y el hash del token recibido
 * antes de aceptarlos.
 */
@Service
public class RefreshTokenService {
  private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);
  private final PasswordEncoder passwordEncoder;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtProps jwtProps;

  public RefreshTokenService(
      @Qualifier("refreshTokenPasswordEncoder") PasswordEncoder passwordEncoder,
      JwtProps jwtProps,
      RefreshTokenRepository refreshTokenRepository) {
    this.passwordEncoder = passwordEncoder;
    this.jwtProps = jwtProps;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  /**
   * Busca un refresh token por su id.
   *
   * @param refreshTokenId el id del refresh token
   * @return la entidad {@link RefreshToken} encontrada
   * @throws org.springframework.web.server.ResponseStatusException con 401 Unauthorized si no
   *     existe
   */
  public RefreshToken findById(UUID refreshTokenId) {
    return findRefreshTokenByIdOrFail(refreshTokenId);
  }

  /**
   * Crea o reemplaza el refresh token del par (principal, dispositivo): si ya existe uno para esa
   * combinación lo actualiza con el nuevo hash y fecha de expiración; si no, crea uno nuevo.
   *
   * @param principal el principal (staff o customer) dueño del token
   * @param deviceId el identificador del dispositivo que solicita el token
   * @param newRefreshToken el valor crudo del nuevo refresh token, que se hashea antes de persistir
   * @return la entidad {@link RefreshToken} guardada
   */
  public RefreshToken save(AuthPrincipal principal, String deviceId, String newRefreshToken) {
    LocalDateTime now = LocalDateTime.now();

    RefreshToken refreshTokenById =
        refreshTokenRepository
            .findByDeviceIdAndPrincipalTypeAndPrincipalId(
                deviceId, principal.getPrincipalType(), principal.getId())
            .orElse(
                RefreshToken.builder()
                    .deviceId(deviceId)
                    .tokenHash(passwordEncoder.encode(newRefreshToken))
                    .principalId(principal.getId())
                    .principalType(principal.getPrincipalType())
                    .expiresOn(now.plus(jwtProps.getRefreshTokenTtlMs(), ChronoUnit.MILLIS))
                    .createdAt(now)
                    .updatedAt(now)
                    .build());

    refreshTokenById.setTokenHash(passwordEncoder.encode(newRefreshToken));
    refreshTokenById.setExpiresOn(now.plus(jwtProps.getRefreshTokenTtlMs(), ChronoUnit.MILLIS));
    refreshTokenById.setLastUsedAt(null);
    refreshTokenById.setCreatedAt(now);
    refreshTokenById.setUpdatedAt(now);
    return refreshTokenRepository.save(refreshTokenById);
  }

  /**
   * Valida el refresh token actual y, si es válido, lo reemplaza por uno nuevo actualizando su
   * hash, expiración y fecha de último uso.
   *
   * @param refreshTokenId el id del refresh token a rotar
   * @param currentRefreshToken el valor crudo del refresh token actual, para validar su hash
   * @param newRefreshToken el valor crudo del nuevo refresh token
   * @return la entidad {@link RefreshToken} actualizada
   * @throws org.springframework.web.server.ResponseStatusException con 401 Unauthorized si el token
   *     no existe, expiró, la sesión superó su vida máxima, o el hash no coincide
   */
  public RefreshToken rotate(
      UUID refreshTokenId, String currentRefreshToken, String newRefreshToken) {
    LocalDateTime now = LocalDateTime.now();
    RefreshToken refreshTokenById = findRefreshTokenByIdOrFail(refreshTokenId);

    validateRefreshToken(currentRefreshToken, refreshTokenById);

    refreshTokenById.setTokenHash(passwordEncoder.encode(newRefreshToken));
    refreshTokenById.setExpiresOn(now.plus(jwtProps.getRefreshTokenTtlMs(), ChronoUnit.MILLIS));
    refreshTokenById.setLastUsedAt(now);
    refreshTokenById.setUpdatedAt(now);
    return refreshTokenRepository.save(refreshTokenById);
  }

  /**
   * Elimina el refresh token si existe y su hash coincide con el valor crudo recibido. Si no existe
   * o el hash no coincide, no hace nada.
   *
   * @param refreshTokenId el id del refresh token a revocar
   * @param rawToken el valor crudo del refresh token, para validar su hash antes de eliminar
   */
  public void revoke(UUID refreshTokenId, String rawToken) {
    refreshTokenRepository
        .findById(refreshTokenId)
        .filter(refreshToken -> passwordEncoder.matches(rawToken, refreshToken.getTokenHash()))
        .ifPresent(refreshTokenRepository::delete);
  }

  /**
   * Valida que el refresh token no haya expirado, que la sesión no haya superado su vida máxima, y
   * que el hash del valor crudo recibido coincida con el almacenado. Si la sesión expiró o el hash
   * no coincide, elimina el token de la base de datos.
   *
   * @param currentRefreshToken el valor crudo del refresh token a validar
   * @param refreshToken la entidad {@link RefreshToken} contra la cual validar
   * @throws org.springframework.web.server.ResponseStatusException con 401 Unauthorized si el token
   *     no es válido por cualquiera de los motivos anteriores
   */
  private void validateRefreshToken(String currentRefreshToken, RefreshToken refreshToken) {
    LocalDateTime now = LocalDateTime.now();
    boolean sessionExpired =
        now.isAfter(
            refreshToken
                .getCreatedAt()
                .plus(jwtProps.getRefreshTokenMaxLifetimeMs(), ChronoUnit.MILLIS));
    boolean tokenExpired = now.isAfter(refreshToken.getExpiresOn());
    boolean hashMismatch =
        !passwordEncoder.matches(currentRefreshToken, refreshToken.getTokenHash());

    if (sessionExpired || hashMismatch) {
      refreshTokenRepository.delete(refreshToken);
    }

    if (sessionExpired || tokenExpired || hashMismatch) {
      log.warn(AppMessages.JWT_REFRESH_TOKEN_IS_NOT_VALID);
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, AppMessages.JWT_REFRESH_TOKEN_IS_NOT_VALID);
    }
  }

  /**
   * Busca un refresh token por su id o lanza una excepción 401 si no existe.
   *
   * @param refreshTokenId el id del refresh token
   * @return la entidad {@link RefreshToken} encontrada
   * @throws org.springframework.web.server.ResponseStatusException con 401 Unauthorized si no
   *     existe
   */
  private RefreshToken findRefreshTokenByIdOrFail(UUID refreshTokenId) {
    return refreshTokenRepository
        .findById(refreshTokenId)
        .orElseThrow(
            () -> {
              log.warn(AppMessages.JWT_REFRESH_TOKEN_IS_NOT_VALID);
              return new ResponseStatusException(
                  HttpStatus.UNAUTHORIZED, AppMessages.JWT_REFRESH_TOKEN_IS_NOT_VALID);
            });
  }
}
