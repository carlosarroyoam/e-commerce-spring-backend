package com.carlosarroyoam.ecommerce.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Representa la respuesta de autenticación: datos básicos del principal, sus roles y los tokens
 * emitidos (el refresh token se omite del JSON y viaja en cookie).
 */
@Getter
@Setter
@Builder
public class AuthResponse {
  private Long id;
  private String fullName;
  private String firstName;
  private String lastName;
  private String email;
  private List<String> roles;
  private String accessToken;
  @JsonIgnore private String refreshToken;
}
