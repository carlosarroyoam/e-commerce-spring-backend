package com.carlosarroyoam.ecommerce.auth;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Representa el valor parseado de la cookie {@code refresh_token} (formato {@code id.rawToken}):
 * el id del refresh token y su valor crudo sin hashear.
 */
@Getter
@Setter
@Builder
class RefreshTokenCookie {
  private final UUID id;
  private final String rawToken;
}
