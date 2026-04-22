package com.carlosarroyoam.ecommerce.auth;

import com.carlosarroyoam.ecommerce.auth.dto.ForgotPasswordRequest;
import com.carlosarroyoam.ecommerce.auth.dto.LoginRequest;
import com.carlosarroyoam.ecommerce.auth.dto.LoginResponse;
import com.carlosarroyoam.ecommerce.auth.dto.RefreshTokenResponse;
import com.carlosarroyoam.ecommerce.auth.dto.ResetPasswordRequest;
import com.carlosarroyoam.ecommerce.auth.entity.RefreshToken;
import com.carlosarroyoam.ecommerce.auth.principal.AuthPrincipal;
import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

  public AuthService(AuthenticationManager authenticationManager,
      StaffDetailsService staffDetailsService, CustomerDetailsService customerDetailsService,
      RefreshTokenService refreshTokenService, TokenService tokenService) {
    this.authenticationManager = authenticationManager;
    this.staffDetailsService = staffDetailsService;
    this.customerDetailsService = customerDetailsService;
    this.refreshTokenService = refreshTokenService;
    this.tokenService = tokenService;
  }

  public LoginResponse login(LoginRequest request) {
    Authentication auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    AuthPrincipal principal = (AuthPrincipal) auth.getPrincipal();

    String accessToken = tokenService.generateAccessToken(principal);
    String refreshToken = tokenService.generateRefreshToken();

    RefreshToken createdRefreshToken = refreshTokenService.save(principal,
        request.getDeviceFingerprint(), refreshToken);

    return LoginResponse.builder()
        .id(principal.getId())
        .fullName(principal.getFullName())
        .firstName(principal.getFirstName())
        .lastName(principal.getLastName())
        .email(principal.getEmail())
        .roles(principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
        .accessToken(accessToken)
        .refreshToken(createdRefreshToken.getId() + "." + refreshToken)
        .build();
  }

  public RefreshTokenResponse refreshToken(String rawRefreshTokenCookie) {
    List<String> refreshTokenParts = Optional.ofNullable(rawRefreshTokenCookie)
        .map(str -> Arrays.asList(StringUtils.tokenizeToStringArray(str, "\\.")))
        .orElse(Collections.emptyList());

    if (refreshTokenParts.isEmpty()) {
      log.warn(AppMessages.JWT_REFRESH_TOKEN_IS_NOT_VALID);
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
          AppMessages.JWT_REFRESH_TOKEN_IS_NOT_VALID);
    }

    UUID refreshTokenId = UUID.fromString(refreshTokenParts.get(0));
    String currentRefreshToken = refreshTokenParts.get(1);

    RefreshToken refreshTokenById = refreshTokenService.findById(refreshTokenId);
    AuthPrincipal principal = switch (refreshTokenById.getPrincipalType()) {
    case STAFF -> (AuthPrincipal) staffDetailsService
        .loadUserById(refreshTokenById.getPrincipalId());
    case CUSTOMER -> (AuthPrincipal) customerDetailsService
        .loadUserById(refreshTokenById.getPrincipalId());
    };

    String accessToken = tokenService.generateAccessToken(principal);
    String refreshToken = tokenService.generateRefreshToken();

    RefreshToken createdRefreshToken = refreshTokenService.rotate(refreshTokenId,
        currentRefreshToken, refreshToken);

    return RefreshTokenResponse.builder()
        .accessToken(accessToken)
        .refreshToken(createdRefreshToken.getId() + "." + refreshToken)
        .build();
  }

  public void revoke(String rawRefreshTokenCookie) {
    List<String> refreshTokenParts = Optional.ofNullable(rawRefreshTokenCookie)
        .map(str -> Arrays.asList(StringUtils.tokenizeToStringArray(str, "\\.")))
        .orElse(Collections.emptyList());

    if (!refreshTokenParts.isEmpty()) {
      UUID refreshTokenId = UUID.fromString(refreshTokenParts.get(0));
      refreshTokenService.revoke(refreshTokenId);
    }
  }

  public void forgotPassword(ForgotPasswordRequest request) {
  }

  public void resetPassword(ResetPasswordRequest request) {
  }
}
