package com.carlosarroyoam.ecommerce.core.config;

import com.carlosarroyoam.ecommerce.core.filter.JwtAuthenticationFilter;
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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final UserDetailsService staffDetailsService;
  private final UserDetailsService customerDetailsService;
  private final CorsProps corsProps;

  public WebSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
      UserDetailsService staffDetailsService, UserDetailsService customerDetailsService,
      CorsProps corsProps) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.staffDetailsService = staffDetailsService;
    this.customerDetailsService = customerDetailsService;
    this.corsProps = corsProps;
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http,
      AuthenticationManager authenticationManager, ObjectMapper mapper) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth.requestMatchers("/auth/**", "/actuator/**")
            .permitAll()
            .anyRequest()
            .authenticated())
        .authenticationManager(authenticationManager)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(
            ex -> ex.authenticationEntryPoint(new CustomAuthenticationEntryPoint(mapper))
                .accessDeniedHandler(new CustomAccessDeniedHandler(mapper)))
        .build();
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
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(corsProps.getAllowedOrigins());
    configuration.setAllowedMethods(corsProps.getAllowedMethods());
    configuration.setAllowedHeaders(corsProps.getAllowedHeaders());

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
