package com.carlosarroyoam.ecommerce.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RefreshTokenRequest {
  private String refreshToken;
}
