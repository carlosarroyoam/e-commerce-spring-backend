package com.carlosarroyoam.ecommerce.core.config;

import com.carlosarroyoam.ecommerce.auth.principal.AuthPrincipal;
import com.carlosarroyoam.ecommerce.auth.principal.AuthPrincipalAuthenticationToken;
import com.carlosarroyoam.ecommerce.auth.principal.AuthPrincipalMapper;
import com.carlosarroyoam.ecommerce.core.filter.CsrfCookieFilter;
import com.carlosarroyoam.ecommerce.core.filter.MdcUserContextFilter;
import com.carlosarroyoam.ecommerce.core.property.CorsProps;
import com.carlosarroyoam.ecommerce.core.property.JwtProps;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuración central de Spring Security: cadena de filtros HTTP, CSRF con cookie de doble envío,
 * CORS, política de sesión sin estado, autenticación como recurso OAuth2 (JWT propio) y los {@link
 * AuthenticationProvider} de STAFF y CUSTOMER combinados en un único {@link AuthenticationManager}.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
  /**
   * Define la cadena de filtros de seguridad HTTP: habilita CSRF de doble envío (excepto en {@code
   * /auth/login}), CORS, cabeceras same-origin, sesiones sin estado, autenticación como recurso
   * OAuth2 vía JWT, manejadores personalizados de errores de autenticación/autorización, y permite
   * sin autenticación las rutas {@code /auth/**} y {@code /actuator/**}. Añade además el {@link
   * MdcUserContextFilter} tras la autenticación del JWT para exponer la identidad del principal en
   * el MDC.
   *
   * @return la {@link SecurityFilterChain} configurada
   */
  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      CsrfTokenRepository csrfTokenRepository,
      CorsConfigurationSource corsConfigurationSource,
      JwtDecoder jwtDecoder,
      Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter,
      AuthenticationEntryPoint authenticationEntryPoint,
      AccessDeniedHandler accessDeniedHandler)
      throws Exception {
    http.csrf(
            csrf ->
                csrf.csrfTokenRepository(csrfTokenRepository)
                    .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                    .ignoringRequestMatchers("/auth/login"))
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .oauth2ResourceServer(
            oauth2 -> {
              oauth2.authenticationEntryPoint(authenticationEntryPoint);
              oauth2.jwt(
                  jwt -> {
                    jwt.decoder(jwtDecoder);
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter);
                  });
            })
        .exceptionHandling(
            ex -> {
              ex.authenticationEntryPoint(authenticationEntryPoint);
              ex.accessDeniedHandler(accessDeniedHandler);
            })
        .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
        .addFilterAfter(new MdcUserContextFilter(), BearerTokenAuthenticationFilter.class);

    http.authorizeHttpRequests(
        auth ->
            auth.requestMatchers("/auth/**")
                .permitAll()
                .requestMatchers("/actuator/**")
                .permitAll()
                .anyRequest()
                .authenticated());

    return http.build();
  }

  /**
   * Codificador de contraseñas usado para las credenciales de acceso de STAFF y CUSTOMER.
   *
   * @return un {@link BCryptPasswordEncoder} con factor de coste 12
   */
  @Bean
  @Primary
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  /**
   * Codificador de bajo coste usado solo para el hash de refresh tokens, donde la comparación se
   * hace en cada petición y un coste alto sería innecesariamente lento.
   *
   * @return un {@link BCryptPasswordEncoder} con factor de coste 4
   */
  @Bean
  @Qualifier("refreshTokenPasswordEncoder")
  PasswordEncoder refreshTokenPasswordEncoder() {
    return new BCryptPasswordEncoder(4);
  }

  /**
   * Proveedor de autenticación para el principal STAFF.
   *
   * @param staffDetailsService el {@link UserDetailsService} de STAFF
   * @param passwordEncoder el codificador de contraseñas a usar
   * @return el {@link AuthenticationProvider} configurado
   */
  @Bean
  AuthenticationProvider staffAuthenticationProvider(
      UserDetailsService staffDetailsService, PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider authenticationProvider =
        new DaoAuthenticationProvider(staffDetailsService);
    authenticationProvider.setPasswordEncoder(passwordEncoder);
    return authenticationProvider;
  }

  /**
   * Proveedor de autenticación para el principal CUSTOMER.
   *
   * @param customerDetailsService el {@link UserDetailsService} de CUSTOMER
   * @param passwordEncoder el codificador de contraseñas a usar
   * @return el {@link AuthenticationProvider} configurado
   */
  @Bean
  AuthenticationProvider customerAuthenticationProvider(
      UserDetailsService customerDetailsService, PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider authenticationProvider =
        new DaoAuthenticationProvider(customerDetailsService);
    authenticationProvider.setPasswordEncoder(passwordEncoder);
    return authenticationProvider;
  }

  /**
   * Combina los proveedores de autenticación de STAFF y CUSTOMER en un único {@link
   * AuthenticationManager}.
   *
   * @param providers los proveedores de autenticación registrados
   * @return el {@link AuthenticationManager} resultante
   */
  @Bean
  AuthenticationManager authenticationManager(List<AuthenticationProvider> providers) {
    return new ProviderManager(providers);
  }

  /**
   * Convierte un {@link Jwt} validado en un {@link AuthPrincipalAuthenticationToken}, mapeando sus
   * claims al {@link AuthPrincipal} correspondiente.
   *
   * @param mapper el mapeador de JWT a {@link AuthPrincipal}
   * @return el {@link Converter} usado por el recurso OAuth2
   */
  @Bean
  Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter(
      AuthPrincipalMapper mapper) {
    return jwt -> {
      AuthPrincipal principal = mapper.map(jwt);
      return new AuthPrincipalAuthenticationToken(jwt, principal, principal.getAuthorities());
    };
  }

  /**
   * Repositorio de tokens CSRF basado en cookie ({@code XSRF-TOKEN}, no {@code HttpOnly}), con el
   * mismo tiempo de vida que el refresh token para que un refresh silencioso tras reabrir el
   * navegador no sea rechazado por falta de cookie CSRF.
   *
   * @param jwtProps propiedades de JWT, usadas para el tiempo de vida de la cookie
   * @return el {@link CookieCsrfTokenRepository} configurado
   */
  @Bean
  CookieCsrfTokenRepository csrfTokenRepository(JwtProps jwtProps) {
    CookieCsrfTokenRepository cookieCsrfTokenRepository =
        CookieCsrfTokenRepository.withHttpOnlyFalse();
    cookieCsrfTokenRepository.setCookiePath("/");
    cookieCsrfTokenRepository.setHeaderName("X-XSRF-TOKEN");
    // Match the refresh_token cookie's lifetime so a silent refresh after the browser was
    // closed and reopened isn't rejected for lacking a (session-lived) CSRF cookie.
    cookieCsrfTokenRepository.setCookieCustomizer(
        cookie -> cookie.maxAge(Duration.ofMillis(jwtProps.getRefreshTokenTtlMs())));
    return cookieCsrfTokenRepository;
  }

  /**
   * Construye la configuración CORS aplicada a todas las rutas a partir de {@link CorsProps}.
   *
   * @param corsProps propiedades de configuración CORS
   * @return el {@link CorsConfigurationSource} resultante
   */
  @Bean
  CorsConfigurationSource corsConfigurationSource(CorsProps corsProps) {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(corsProps.getAllowedOrigins());
    configuration.setAllowedMethods(corsProps.getAllowedMethods());
    configuration.setAllowedHeaders(corsProps.getAllowedHeaders());
    configuration.setExposedHeaders(corsProps.getExposedHeaders());
    configuration.setAllowCredentials(corsProps.getAllowCredentials());

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
