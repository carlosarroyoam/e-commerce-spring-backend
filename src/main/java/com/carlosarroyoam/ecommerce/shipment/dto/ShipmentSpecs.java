package com.carlosarroyoam.ecommerce.shipment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ShipmentSpecs {
  private Long orderId;
}
