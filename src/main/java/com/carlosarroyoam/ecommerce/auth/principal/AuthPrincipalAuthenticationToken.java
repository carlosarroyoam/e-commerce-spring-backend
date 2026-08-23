package com.carlosarroyoam.ecommerce.auth.principal;

import java.util.Collection;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;

public class AuthPrincipalAuthenticationToken extends AbstractOAuth2TokenAuthenticationToken<Jwt> {
  private static final long serialVersionUID = 262645319780897810L;

  public AuthPrincipalAuthenticationToken(
      Jwt jwt, AuthPrincipal principal, Collection<? extends GrantedAuthority> authorities) {
    super(jwt, principal, jwt, authorities);
    setAuthenticated(true);
  }

  @Override
  public Map<String, Object> getTokenAttributes() {
    return getToken().getClaims();
  }
}
