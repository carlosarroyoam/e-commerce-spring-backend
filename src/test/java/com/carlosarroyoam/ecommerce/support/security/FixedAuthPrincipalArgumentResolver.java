package com.carlosarroyoam.ecommerce.support.security;

import com.carlosarroyoam.ecommerce.auth.principal.AuthPrincipal;
import com.carlosarroyoam.ecommerce.auth.principal.PrincipalType;
import java.util.Set;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link HandlerMethodArgumentResolver} de prueba que resuelve cualquier parametro {@link
 * AuthPrincipal} con un principal fijo, para usar en {@code MockMvc.standaloneSetup(...)} donde no
 * hay {@code SecurityContextHolder} poblado ni method-security activo.
 */
public class FixedAuthPrincipalArgumentResolver implements HandlerMethodArgumentResolver {
  private final AuthPrincipal fixedPrincipal;

  public FixedAuthPrincipalArgumentResolver(long id, PrincipalType principalType, String... roles) {
    this.fixedPrincipal =
        AuthPrincipal.builder().id(id).principalType(principalType).roles(Set.of(roles)).build();
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return AuthPrincipal.class.isAssignableFrom(parameter.getParameterType());
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {
    return fixedPrincipal;
  }
}
