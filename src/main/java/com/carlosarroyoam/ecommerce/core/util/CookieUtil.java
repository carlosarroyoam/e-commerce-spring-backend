package com.carlosarroyoam.ecommerce.core.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {
  public ResponseCookie createCookie(String cookieName, String cookieValue, Duration duration) {
    return ResponseCookie.from(cookieName, cookieValue)
        .httpOnly(true)
        .secure(false)
        .sameSite("Strict")
        .path("/")
        .maxAge(duration)
        .build();
  }

  public ResponseCookie deleteCookie(String cookieName) {
    return ResponseCookie.from(cookieName, Strings.EMPTY)
        .httpOnly(true)
        .secure(false)
        .sameSite("Strict")
        .path("/")
        .maxAge(Duration.ofMillis(0))
        .build();
  }

  public String getCookie(HttpServletRequest request, String cookieName) {
    if (request.getCookies() == null) {
      return null;
    }

    return Arrays.stream(request.getCookies())
        .filter(c -> cookieName.equals(c.getName()))
        .findFirst()
        .map(Cookie::getValue)
        .orElseGet(() -> null);
  }
}
