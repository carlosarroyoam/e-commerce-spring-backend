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

  public RefreshTokenService(PasswordEncoder passwordEncoder, JwtProps jwtProps,
      RefreshTokenRepository refreshTokenRepository) {
    this.passwordEncoder = passwordEncoder;
    this.jwtProps = jwtProps;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  public RefreshToken findById(UUID refreshTokenId) {
    return findRefreshTokenByIdOrFail(refreshTokenId);
  }

  public RefreshToken save(AuthPrincipal principal, String fingerprint, String newRefreshToken) {
    LocalDateTime now = LocalDateTime.now();

    RefreshToken refreshTokenById = refreshTokenRepository
        .findByFingerprintAndPrincipalType(fingerprint, principal.getPrincipalType())
        .orElse(RefreshToken.builder()
            .fingerprint(fingerprint)
            .token(passwordEncoder.encode(newRefreshToken))
            .principalId(principal.getId())
            .principalType(principal.getPrincipalType())
            .expiresOn(now.plus(jwtProps.getRefreshTokenTtlMs(), ChronoUnit.MILLIS))
            .createdAt(now)
            .updatedAt(now)
            .build());

    refreshTokenById.setToken(passwordEncoder.encode(newRefreshToken));
    refreshTokenById.setExpiresOn(now.plus(jwtProps.getRefreshTokenTtlMs(), ChronoUnit.MILLIS));
    refreshTokenById.setLastUsedAt(null);
    refreshTokenById.setCreatedAt(now);
    refreshTokenById.setUpdatedAt(now);
    return refreshTokenRepository.save(refreshTokenById);
  }

  public RefreshToken rotate(UUID refreshTokenId, String currentRefreshToken,
      String newRefreshToken) {
    LocalDateTime now = LocalDateTime.now();
    RefreshToken refreshTokenById = findRefreshTokenByIdOrFail(refreshTokenId);

    validateRefreshToken(currentRefreshToken, refreshTokenById);

    refreshTokenById.setToken(passwordEncoder.encode(newRefreshToken));
    refreshTokenById.setExpiresOn(now.plus(jwtProps.getRefreshTokenTtlMs(), ChronoUnit.MILLIS));
    refreshTokenById.setLastUsedAt(now);
    refreshTokenById.setUpdatedAt(now);
    return refreshTokenRepository.save(refreshTokenById);
  }

  public void revoke(UUID refreshTokenId) {
    refreshTokenRepository.findById(refreshTokenId).ifPresent(refreshTokenRepository::delete);
  }

  private void validateRefreshToken(String currentRefreshToken, RefreshToken refreshToken) {
    if (!passwordEncoder.matches(currentRefreshToken, refreshToken.getToken())) {
      log.warn(AppMessages.TOKEN_IS_NOT_VALID);
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, AppMessages.TOKEN_IS_NOT_VALID);
    }

    if (LocalDateTime.now().isAfter(refreshToken.getExpiresOn())) {
      log.warn(AppMessages.TOKEN_IS_NOT_VALID);
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, AppMessages.TOKEN_IS_NOT_VALID);
    }
  }

  private RefreshToken findRefreshTokenByIdOrFail(UUID refreshTokenId) {
    return refreshTokenRepository.findById(refreshTokenId).orElseThrow(() -> {
      log.warn(AppMessages.TOKEN_IS_NOT_VALID);
      return new ResponseStatusException(HttpStatus.UNAUTHORIZED, AppMessages.TOKEN_IS_NOT_VALID);
    });
  }
}
