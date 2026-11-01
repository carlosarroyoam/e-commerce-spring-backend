package com.carlosarroyoam.ecommerce.shipment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/** Agrupa los filtros opcionales para consultar el listado de envíos. */
@Getter
@Setter
@Builder
public class ShipmentSpecs {
  private Long orderId;
}
