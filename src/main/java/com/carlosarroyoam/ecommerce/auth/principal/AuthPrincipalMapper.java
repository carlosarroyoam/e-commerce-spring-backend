package com.carlosarroyoam.ecommerce.auth.principal;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthPrincipalMapper {
  public AuthPrincipal map(Jwt jwt) {
    Set<String> roles =
        Optional.ofNullable(jwt.getClaimAsStringList("roles")).orElseGet(List::of).stream()
            .collect(Collectors.toUnmodifiableSet());

    return AuthPrincipal.builder()
        .id(Long.valueOf(jwt.getSubject()))
        .fullName(jwt.getClaimAsString("name"))
        .firstName(jwt.getClaimAsString("given_name"))
        .lastName(jwt.getClaimAsString("family_name"))
        .email(jwt.getClaimAsString("email"))
        .principalType(PrincipalType.valueOf(jwt.getClaimAsString("principal_type")))
        .roles(roles)
        .build();
  }
}
