package com.carlosarroyoam.ecommerce.auth.principal;

import java.io.Serial;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@AllArgsConstructor
@Builder
public class AuthPrincipal implements UserDetails {
  @Serial
  private static final long serialVersionUID = -156588274831556503L;
  private final Long id;
  private final String fullName;
  private final String firstName;
  private final String lastName;
  private final String email;
  private final String passwordHash;
  private final String status;
  private final PrincipalType principalType;
  private final Set<String> roles;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return roles.stream()
        .map(role -> "ROLE_" + role)
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public String getPassword() {
    return passwordHash;
  }

  @Override
  public boolean isEnabled() {
    return status.equals("ACTIVE");
  }
}
