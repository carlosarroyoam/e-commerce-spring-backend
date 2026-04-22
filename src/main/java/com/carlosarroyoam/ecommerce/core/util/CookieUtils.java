package com.carlosarroyoam.ecommerce.core.util;

import java.time.Duration;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {
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
}
