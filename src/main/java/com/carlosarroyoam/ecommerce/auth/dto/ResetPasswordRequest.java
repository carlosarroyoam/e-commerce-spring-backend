package com.carlosarroyoam.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ResetPasswordRequest {
  @NotBlank(message = "Password must not be blank")
  private String password;

  @NotBlank(message = "Confirm password must not be blank")
  private String confirmPassword;
}
