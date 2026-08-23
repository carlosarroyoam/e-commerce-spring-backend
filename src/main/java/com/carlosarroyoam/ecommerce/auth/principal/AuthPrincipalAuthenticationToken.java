package com.carlosarroyoam.ecommerce.auth.principal;

import java.io.Serial;
import java.util.Collection;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;

/**
 * Token de autenticación de Spring Security que envuelve el {@link Jwt} recibido junto con el
 * {@link AuthPrincipal} reconstruido a partir de sus claims.
 */
public class AuthPrincipalAuthenticationToken extends AbstractOAuth2TokenAuthenticationToken<Jwt> {
  @Serial
  private static final long serialVersionUID = 262645319780897810L;

  /**
   * Crea el token de autenticación ya marcado como autenticado.
   *
   * @param jwt el token JWT decodificado
   * @param principal el principal reconstruido a partir del JWT
   * @param authorities las authorities otorgadas al principal
   */
  public AuthPrincipalAuthenticationToken(
      Jwt jwt, AuthPrincipal principal, Collection<? extends GrantedAuthority> authorities) {
    super(jwt, principal, jwt, authorities);
    setAuthenticated(true);
  }

  /**
   * Devuelve los claims del JWT como atributos del token de autenticación.
   *
   * @return los claims del JWT
   */
  @Override
  public Map<String, Object> getTokenAttributes() {
    return getToken().getClaims();
  }
}
