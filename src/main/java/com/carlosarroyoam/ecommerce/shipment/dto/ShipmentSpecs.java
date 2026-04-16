package com.carlosarroyoam.ecommerce.shipment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShipmentSpecs {
  private Long orderId;
}
