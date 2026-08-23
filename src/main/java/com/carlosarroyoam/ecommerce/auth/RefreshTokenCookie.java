package com.carlosarroyoam.ecommerce.auth;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
class RefreshTokenCookie {
  private final UUID id;
  private final String rawToken;
}
