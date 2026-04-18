package com.carlosarroyoam.ecommerce.core.filter;

import com.carlosarroyoam.ecommerce.auth.TokenService;
import com.carlosarroyoam.ecommerce.auth.principal.PrincipalType;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
  private final TokenService tokenService;
  private final UserDetailsService staffDetailsService;
  private final UserDetailsService customerDetailsService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    String token = extractBearerToken(request);

    if (token == null) {
      chain.doFilter(request, response);
      return;
    }

    try {
      PrincipalType principalType = tokenService.extractType(token);
      String email = tokenService.extractEmail(token);

      if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails principal = switch (principalType) {
        case STAFF -> staffDetailsService.loadUserByUsername(email);
        case CUSTOMER -> customerDetailsService.loadUserByUsername(email);
        };

        if (tokenService.isValid(token, principal)) {
          var auth = new UsernamePasswordAuthenticationToken(principal, null,
              principal.getAuthorities());
          auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(auth);
        }
      }
    } catch (JwtException ex) {
      log.warn("JwtException: {}", ex.getMessage());
    }

    chain.doFilter(request, response);
  }

  private String extractBearerToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");

    if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
      return null;
    }

    return header.substring(7);
  }
}
