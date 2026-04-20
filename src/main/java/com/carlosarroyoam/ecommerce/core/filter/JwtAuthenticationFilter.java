package com.carlosarroyoam.ecommerce.core.filter;

import com.carlosarroyoam.ecommerce.auth.TokenService;
import com.carlosarroyoam.ecommerce.auth.principal.PrincipalType;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private final UserDetailsService staffDetailsService;
  private final UserDetailsService customerDetailsService;
  private final TokenService tokenService;

  public JwtAuthenticationFilter(UserDetailsService staffDetailsService,
      UserDetailsService customerDetailsService, TokenService tokenService) {
    this.staffDetailsService = staffDetailsService;
    this.customerDetailsService = customerDetailsService;
    this.tokenService = tokenService;
  }

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    Optional<String> token = extractBearerToken(request);

    if (token.isEmpty()) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      PrincipalType principalType = tokenService.extractType(token.get());
      String email = tokenService.extractEmail(token.get());

      UserDetails principal = switch (principalType) {
      case STAFF -> staffDetailsService.loadUserByUsername(email);
      case CUSTOMER -> customerDetailsService.loadUserByUsername(email);
      };

      if (tokenService.isValid(token.get(), principal)) {
        var auth = new UsernamePasswordAuthenticationToken(principal, null,
            principal.getAuthorities());
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
      }
    } catch (JwtException ex) {
      log.warn("JwtException: {}", ex.getMessage());
    }

    filterChain.doFilter(request, response);
  }

  private Optional<String> extractBearerToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");

    if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
      return Optional.empty();
    }

    return Optional.of(header.substring(7));
  }
}
