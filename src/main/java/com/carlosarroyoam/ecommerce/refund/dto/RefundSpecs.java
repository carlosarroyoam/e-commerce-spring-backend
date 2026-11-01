package com.carlosarroyoam.ecommerce.refund.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/** Agrupa los filtros opcionales para consultar el listado de reembolsos. */
@Getter
@Setter
@Builder
public class RefundSpecs {
  private Long orderId;
}
