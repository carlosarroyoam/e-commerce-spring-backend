package com.carlosarroyoam.ecommerce.auth.principal;

import java.io.Serial;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Representa al principal autenticado (staff o customer) de forma unificada para Spring Security,
 * independientemente de su tabla de origen.
 */
@Getter
@Setter
@Builder
public class AuthPrincipal implements UserDetails {
  @Serial private static final long serialVersionUID = -156588274831556503L;
  private final Long id;
  private final String fullName;
  private final String firstName;
  private final String lastName;
  private final String email;
  private final String passwordHash;
  private final String status;
  private final PrincipalType principalType;
  private final Set<String> roles;

  /**
   * Convierte los roles del principal en authorities de Spring Security, prefijando cada uno con
   * {@code ROLE_}.
   *
   * @return las authorities del principal
   */
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return roles.stream()
        .map(role -> "ROLE_" + role)
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());
  }

  /**
   * Devuelve el nombre de usuario de Spring Security, que en este proyecto es el email.
   *
   * @return el email del principal
   */
  @Override
  public String getUsername() {
    return email;
  }

  /**
   * Devuelve el hash de la contraseña del principal, usado por Spring Security para validar
   * credenciales.
   *
   * @return el hash de la contraseña
   */
  @Override
  public String getPassword() {
    return passwordHash;
  }

  /**
   * Indica si el principal está habilitado para autenticarse.
   *
   * @return {@code true} si el estado del principal es {@code ACTIVE}
   */
  @Override
  public boolean isEnabled() {
    return "ACTIVE".equals(status);
  }
}
