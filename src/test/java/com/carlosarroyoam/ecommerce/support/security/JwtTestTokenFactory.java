package com.carlosarroyoam.ecommerce.support.security;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

/**
 * Firma JWT de acceso reales (mismo shape que {@code TokenService.generateAccessToken}) usando el
 * {@link JwtEncoder} de la aplicacion, para ejercitar el pipeline de seguridad completo
 * (decodificador, validador de emisor/firma, {@code AuthPrincipalMapper}) en tests sin mockear
 * nada de seguridad.
 *
 * <p>Anotada con {@link TestComponent} (no {@code @Component}) para que nunca sea recogida por el
 * escaneo de componentes de la aplicacion real, ya que vive bajo el mismo paquete base que {@code
 * ECommerceApplication}; solo se registra cuando un test la importa explicitamente (ver {@link
 * SecurityTestConfig} y {@code AbstractIntegrationTest}).
 */
@TestComponent
public class JwtTestTokenFactory {
  private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);

  private final JwtEncoder jwtEncoder;

  public JwtTestTokenFactory(JwtEncoder jwtEncoder) {
    this.jwtEncoder = jwtEncoder;
  }

  /**
   * Firma un JWT de acceso valido para un principal STAFF.
   *
   * @param id el id del usuario staff
   * @param email el email del usuario staff
   * @param roles los roles (sin el prefijo {@code ROLE_}) a incluir en el claim {@code roles}
   * @return el JWT firmado como cadena compacta
   */
  public String staffToken(long id, String email, String... roles) {
    return token(id, email, "STAFF", DEFAULT_TTL, roles);
  }

  /**
   * Firma un JWT de acceso valido para un principal CUSTOMER.
   *
   * @param id el id del cliente
   * @param email el email del cliente
   * @param roles los roles (sin el prefijo {@code ROLE_}) a incluir en el claim {@code roles}
   * @return el JWT firmado como cadena compacta
   */
  public String customerToken(long id, String email, String... roles) {
    return token(id, email, "CUSTOMER", DEFAULT_TTL, roles);
  }

  /**
   * Firma un JWT ya expirado, util para probar el camino de error 401 de {@code
   * CustomAuthenticationEntryPoint}.
   *
   * @param id el id del principal
   * @param email el email del principal
   * @param principalType {@code STAFF} o {@code CUSTOMER}
   * @param roles los roles a incluir en el claim {@code roles}
   * @return el JWT firmado, ya expirado, como cadena compacta
   */
  public String expiredToken(long id, String email, String principalType, String... roles) {
    return token(id, email, principalType, Duration.ofSeconds(-10), roles);
  }

  private String token(
      long id, String email, String principalType, Duration ttl, String... roles) {
    Instant now = Instant.now();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer("self")
            .subject(String.valueOf(id))
            .claim("email", email)
            .claim("name", "Test User")
            .claim("given_name", "Test")
            .claim("family_name", "User")
            .claim("principal_type", principalType)
            .claim("roles", List.of(roles))
            .issuedAt(now)
            .expiresAt(now.plus(ttl))
            .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }
}
