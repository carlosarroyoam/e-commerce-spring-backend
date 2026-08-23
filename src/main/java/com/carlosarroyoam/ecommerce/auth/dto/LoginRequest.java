package com.carlosarroyoam.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginRequest {
  @NotBlank(message = "Email must not be blank")
  private String email;

  @NotBlank(message = "Password must not be blank")
  private String password;

  @NotBlank(message = "Device id must not be blank")
  @Size(max = 36, message = "Device id should be max 36")
  private String deviceId;
}
