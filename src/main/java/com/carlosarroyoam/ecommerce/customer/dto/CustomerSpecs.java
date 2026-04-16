package com.carlosarroyoam.ecommerce.customer.dto;

import com.carlosarroyoam.ecommerce.customer.entity.CustomerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@Builder
public class CustomerSpecs {
  @Size(max = 64, message = "First name should be max 64")
  private String firstName;

  @Size(max = 64, message = "Last name should be max 64")
  private String lastName;

  @Email(message = "Email should be an valid email address")
  @Size(min = 3, max = 128, message = "Email should be between 3 and 128")
  private String email;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate startDate;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate endDate;

  private CustomerStatus status;
}
