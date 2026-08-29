package com.carlosarroyoam.ecommerce.core.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.carlosarroyoam.ecommerce.auth.principal.AuthPrincipal;
import com.carlosarroyoam.ecommerce.auth.principal.PrincipalType;
import jakarta.servlet.FilterChain;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

/** Pruebas unitarias de {@link MdcUserContextFilter}. */
class MdcUserContextFilterTest {
  private final MdcUserContextFilter filter = new MdcUserContextFilter();

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
    MDC.clear();
  }

  private static AuthPrincipal staffPrincipal() {
    return AuthPrincipal.builder()
        .id(42L)
        .email("staff@example.com")
        .principalType(PrincipalType.STAFF)
        .roles(Set.of("ADMIN"))
        .build();
  }

  private static void authenticate(Object principal) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                principal, null, AuthorityUtils.NO_AUTHORITIES));
  }

  /** Ejecuta el filtro y devuelve el valor de las claves MDC de usuario visto durante la cadena. */
  private Map<String, String> runFilterAndCaptureMdc() throws Exception {
    AtomicReference<Map<String, String>> captured = new AtomicReference<>(Map.of());
    FilterChain chain =
        (req, res) -> {
          Map<String, String> values = new HashMap<>();
          values.put(MdcUserContextFilter.USER_ID_KEY, MDC.get(MdcUserContextFilter.USER_ID_KEY));
          values.put(
              MdcUserContextFilter.PRINCIPAL_TYPE_KEY,
              MDC.get(MdcUserContextFilter.PRINCIPAL_TYPE_KEY));
          captured.set(values);
        };

    filter.doFilter(
        new MockHttpServletRequest("GET", "/users"), new MockHttpServletResponse(), chain);
    return captured.get();
  }

  @Test
  @DisplayName(
      "Given an authenticated AuthPrincipal, when doFilter, then userId and principalType"
          + " are set in the MDC during the chain")
  void givenAuthenticatedAuthPrincipal_whenDoFilter_thenUserIdAndPrincipalTypeInMdc()
      throws Exception {
    authenticate(staffPrincipal());

    Map<String, String> mdcDuringChain = runFilterAndCaptureMdc();

    assertThat(mdcDuringChain.get(MdcUserContextFilter.USER_ID_KEY)).isEqualTo("42");
    assertThat(mdcDuringChain.get(MdcUserContextFilter.PRINCIPAL_TYPE_KEY)).isEqualTo("STAFF");
  }

  @Test
  @DisplayName("Given no authentication, when doFilter, then no user keys are set in the MDC")
  void givenNoAuthentication_whenDoFilter_thenNoUserKeysInMdc() throws Exception {
    Map<String, String> mdcDuringChain = runFilterAndCaptureMdc();

    assertThat(mdcDuringChain.get(MdcUserContextFilter.USER_ID_KEY)).isNull();
    assertThat(mdcDuringChain.get(MdcUserContextFilter.PRINCIPAL_TYPE_KEY)).isNull();
  }

  @Test
  @DisplayName(
      "Given an anonymous authentication, when doFilter, then no user keys are set in the" + " MDC")
  void givenAnonymousAuthentication_whenDoFilter_thenNoUserKeysInMdc() throws Exception {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

    Map<String, String> mdcDuringChain = runFilterAndCaptureMdc();

    assertThat(mdcDuringChain.get(MdcUserContextFilter.USER_ID_KEY)).isNull();
    assertThat(mdcDuringChain.get(MdcUserContextFilter.PRINCIPAL_TYPE_KEY)).isNull();
  }

  @Test
  @DisplayName(
      "Given an authenticated AuthPrincipal, when the filter completes, then the user keys"
          + " are removed from the MDC")
  void givenAuthenticated_whenFilterCompletes_thenUserKeysAreRemovedFromMdc() throws Exception {
    authenticate(staffPrincipal());

    filter.doFilter(
        new MockHttpServletRequest("GET", "/users"),
        new MockHttpServletResponse(),
        (req, res) -> {});

    assertThat(MDC.get(MdcUserContextFilter.USER_ID_KEY)).isNull();
    assertThat(MDC.get(MdcUserContextFilter.PRINCIPAL_TYPE_KEY)).isNull();
  }
}
