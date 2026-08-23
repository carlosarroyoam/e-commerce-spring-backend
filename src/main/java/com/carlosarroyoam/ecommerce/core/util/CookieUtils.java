package com.carlosarroyoam.ecommerce.core.util;

import com.carlosarroyoam.ecommerce.core.property.CookieProps;
import java.time.Duration;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {
  private final CookieProps cookieProps;

  public CookieUtils(CookieProps cookieProps) {
    this.cookieProps = cookieProps;
  }

  public ResponseCookie createCookie(String cookieName, String cookieValue, Duration duration) {
    return ResponseCookie.from(cookieName, cookieValue)
        .httpOnly(true)
        .secure(cookieProps.getSecure())
        .sameSite("Strict")
        .path("/")
        .maxAge(duration)
        .build();
  }

  public ResponseCookie deleteCookie(String cookieName) {
    return ResponseCookie.from(cookieName, Strings.EMPTY)
        .httpOnly(true)
        .secure(cookieProps.getSecure())
        .sameSite("Strict")
        .path("/")
        .maxAge(Duration.ofMillis(0))
        .build();
  }
}
