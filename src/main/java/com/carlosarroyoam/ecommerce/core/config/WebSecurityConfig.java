package com.carlosarroyoam.ecommerce.core.config;

import com.carlosarroyoam.ecommerce.core.filter.CsrfCookieFilter;
import com.carlosarroyoam.ecommerce.core.property.CorsProps;
import com.carlosarroyoam.ecommerce.core.security.CustomAccessDeniedHandler;
import com.carlosarroyoam.ecommerce.core.security.CustomAuthenticationEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
  private final UserDetailsService staffDetailsService;
  private final UserDetailsService customerDetailsService;
  private final CorsProps corsProps;

  public WebSecurityConfig(UserDetailsService staffDetailsService,
      UserDetailsService customerDetailsService, CorsProps corsProps) {
    this.staffDetailsService = staffDetailsService;
    this.customerDetailsService = customerDetailsService;
    this.corsProps = corsProps;
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http,
      CookieCsrfTokenRepository csrfTokenRepository, AuthenticationManager authenticationManager,
      JwtDecoder jwtDecoder, JwtAuthenticationConverter jwtAuthenticationConverter,
      ObjectMapper mapper) throws Exception {
    http.csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository)
        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
        .ignoringRequestMatchers("/auth/login"))
        .cors(Customizer.withDefaults())
        .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
          jwt.decoder(jwtDecoder);
          jwt.jwtAuthenticationConverter(jwtAuthenticationConverter);
        }))
        .exceptionHandling(ex -> {
          ex.authenticationEntryPoint(new CustomAuthenticationEntryPoint(mapper));
          ex.accessDeniedHandler(new CustomAccessDeniedHandler(mapper));
        })
        .addFilterBefore(new CsrfCookieFilter(), CsrfFilter.class);

    http.authorizeHttpRequests(auth -> auth.requestMatchers("/auth/**", "/actuator/**")
        .permitAll()
        .anyRequest()
        .authenticated());

    return http.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  AuthenticationProvider staffAuthenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(staffDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  @Bean
  AuthenticationProvider customerAuthenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customerDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  @Bean
  AuthenticationManager authenticationManager(List<AuthenticationProvider> providers) {
    return new ProviderManager(providers);
  }

  @Bean
  JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
    authoritiesConverter.setAuthoritiesClaimName("roles");
    authoritiesConverter.setAuthorityPrefix("ROLE_");

    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
    return converter;
  }

  @Bean
  CookieCsrfTokenRepository csrfTokenRepository() {
    CookieCsrfTokenRepository cookieCsrfTokenRepository = CookieCsrfTokenRepository
        .withHttpOnlyFalse();
    cookieCsrfTokenRepository.setCookiePath("/");
    cookieCsrfTokenRepository.setHeaderName("X-XSRF-TOKEN");
    return cookieCsrfTokenRepository;
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(corsProps.getAllowedOrigins());
    configuration.setAllowedMethods(corsProps.getAllowedMethods());
    configuration.setAllowedHeaders(corsProps.getAllowedHeaders());
    configuration.setAllowCredentials(corsProps.getAllowCredentials());

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
