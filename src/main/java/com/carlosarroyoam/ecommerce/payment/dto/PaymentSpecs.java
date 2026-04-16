package com.carlosarroyoam.ecommerce.payment.dto;

import com.carlosarroyoam.ecommerce.payment.entity.PaymentMethod;
import com.carlosarroyoam.ecommerce.payment.entity.PaymentStatus;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@Builder
public class PaymentSpecs {
  @Size(max = 255, message = "Reference should be max 255")
  private String reference;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate startDate;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate endDate;

  private PaymentMethod method;
  private PaymentStatus status;
  private Long orderId;
}
