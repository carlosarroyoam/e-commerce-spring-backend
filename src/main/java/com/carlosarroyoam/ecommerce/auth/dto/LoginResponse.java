package com.carlosarroyoam.ecommerce.auth.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
  private Long id;
  private String fullName;
  private String firstName;
  private String lastName;
  private String email;
  private List<String> roles;
  private String accessToken;
  private String refreshToken;
}
