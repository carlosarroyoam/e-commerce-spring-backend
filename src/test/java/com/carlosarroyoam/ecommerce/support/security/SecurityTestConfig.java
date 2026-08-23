package com.carlosarroyoam.ecommerce.support.security;

import com.carlosarroyoam.ecommerce.auth.principal.AuthPrincipalMapper;
import com.carlosarroyoam.ecommerce.core.config.JwtConfig;
import com.carlosarroyoam.ecommerce.core.config.WebSecurityConfig;
import com.carlosarroyoam.ecommerce.core.exception.ApiExceptionResponseFactory;
import com.carlosarroyoam.ecommerce.core.property.CookieProps;
import com.carlosarroyoam.ecommerce.core.property.CorsProps;
import com.carlosarroyoam.ecommerce.core.property.JwtProps;
import com.carlosarroyoam.ecommerce.core.property.RsaKeyProps;
import com.carlosarroyoam.ecommerce.core.security.CustomAccessDeniedHandler;
import com.carlosarroyoam.ecommerce.core.security.CustomAuthenticationEntryPoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Reconstituye el pipeline de seguridad real ({@link WebSecurityConfig}, {@link JwtConfig} y sus
 * dependencias) dentro de un slice {@code @WebMvcTest}, que por defecto no escanea beans {@code
 * @Component} genericos ni registra las clases {@code @ConfigurationProperties} (que en este
 * proyecto son {@code @Component} sueltos, no via {@code @EnableConfigurationProperties}).
 *
 * <p>Se importa explicitamente en cada {@code @WebMvcTest} que necesite ejercitar autenticacion
 * real (JWT firmado con {@link JwtTestTokenFactory}). Anotada con {@link TestConfiguration} (no
 * {@code @Configuration}) para que, pese a vivir bajo el mismo paquete base que {@code
 * ECommerceApplication}, nunca sea recogida por el escaneo de componentes de la aplicacion real
 * en un {@code @SpringBootTest} de contexto completo.
 *
 * <p>Los beans {@code staffDetailsService}/{@code customerDetailsService} son stubs: los tests de
 * slice no ejercitan {@code /auth/login} (eso es responsabilidad de los tests de {@code auth}),
 * solo necesitan existir para que {@link WebSecurityConfig} pueda construir sus {@code
 * AuthenticationProvider}.
 */
@TestConfiguration
@Import({
  WebSecurityConfig.class,
  JwtConfig.class,
  AuthPrincipalMapper.class,
  ApiExceptionResponseFactory.class,
  CustomAuthenticationEntryPoint.class,
  CustomAccessDeniedHandler.class
})
@EnableConfigurationProperties({CorsProps.class, JwtProps.class, RsaKeyProps.class, CookieProps.class})
public class SecurityTestConfig {
  @Bean("staffDetailsService")
  UserDetailsService staffDetailsService() {
    return email -> {
      throw new UsernameNotFoundException(email);
    };
  }

  @Bean("customerDetailsService")
  UserDetailsService customerDetailsService() {
    return email -> {
      throw new UsernameNotFoundException(email);
    };
  }
}
