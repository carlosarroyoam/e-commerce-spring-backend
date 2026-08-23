package com.carlosarroyoam.ecommerce.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ForgotPasswordRequest {
  @NotBlank(message = "Email must not be blank")
  @Email(message = "Email should be an valid email address")
  private String email;
}
