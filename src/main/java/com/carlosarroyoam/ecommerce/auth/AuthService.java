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

  public void revoke(String rawRefreshTokenCookie) {
    parseRefreshTokenCookie(rawRefreshTokenCookie)
        .ifPresent(
            refreshTokenCookie ->
                refreshTokenService.revoke(
                    refreshTokenCookie.getId(), refreshTokenCookie.getRawToken()));
  }

  public void forgotPassword(ForgotPasswordRequest request) {
    throw new ResponseStatusException(
        HttpStatus.NOT_IMPLEMENTED, AppMessages.FEATURE_NOT_IMPLEMENTED_EXCEPTION);
  }

  public void resetPassword(ResetPasswordRequest request) {
    throw new ResponseStatusException(
        HttpStatus.NOT_IMPLEMENTED, AppMessages.FEATURE_NOT_IMPLEMENTED_EXCEPTION);
  }

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
