package com.carlosarroyoam.ecommerce.core.property;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "application.cookie")
@Getter
@Setter
public class CookieProps {
  @NotNull(message = "secure must not be null")
  private Boolean secure;
}
