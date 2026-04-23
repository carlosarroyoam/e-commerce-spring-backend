package com.carlosarroyoam.ecommerce.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginRequest {
  private String email;
  private String password;
  private String deviceFingerprint;
}
