package com.carlosarroyoam.ecommerce.payment.dto;

import com.carlosarroyoam.ecommerce.payment.entity.PaymentMethod;
import com.carlosarroyoam.ecommerce.payment.entity.PaymentStatus;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

/** Agrupa los filtros opcionales para consultar el listado de pagos. */
@Getter
@Setter
@Builder
public class PaymentSpecs {
  @Size(max = 254, message = "Reference should be max 254")
  private String reference;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate startDate;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate endDate;

  private PaymentMethod method;
  private PaymentStatus status;
  private Long orderId;
}
