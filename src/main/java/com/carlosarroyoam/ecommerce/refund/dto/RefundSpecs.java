package com.carlosarroyoam.ecommerce.refund.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefundSpecs {
  private Long orderId;
}
