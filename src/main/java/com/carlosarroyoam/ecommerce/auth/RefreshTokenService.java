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

  public RefreshToken findById(UUID refreshTokenId) {
    return findRefreshTokenByIdOrFail(refreshTokenId);
  }

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

  public void revoke(UUID refreshTokenId, String rawToken) {
    refreshTokenRepository
        .findById(refreshTokenId)
        .filter(refreshToken -> passwordEncoder.matches(rawToken, refreshToken.getTokenHash()))
        .ifPresent(refreshTokenRepository::delete);
  }

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
