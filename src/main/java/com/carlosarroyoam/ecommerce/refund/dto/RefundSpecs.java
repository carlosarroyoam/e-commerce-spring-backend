package com.carlosarroyoam.ecommerce.refund.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RefundSpecs {
  private Long orderId;
}
