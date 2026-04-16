package com.carlosarroyoam.ecommerce.shipment;

import com.carlosarroyoam.ecommerce.core.dto.PagedResponse;
import com.carlosarroyoam.ecommerce.shipment.dto.CarrierResponse;
import com.carlosarroyoam.ecommerce.shipment.dto.ShipmentResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shipments")
public class ShipmentController {
  private final ShipmentService shipmentService;

  public ShipmentController(final ShipmentService shipmentService) {
    this.shipmentService = shipmentService;
  }

  @GetMapping(produces = "application/json")
  public ResponseEntity<PagedResponse<ShipmentResponse>> findAll(
      @PageableDefault(page = 0, size = 25, sort = "id") Pageable pageable) {
    PagedResponse<ShipmentResponse> shipments = shipmentService.findAll(pageable);
    return ResponseEntity.ok(shipments);
  }

  @GetMapping(value = "/carriers", produces = "application/json")
  public ResponseEntity<List<CarrierResponse>> findAllCarriers() {
    List<CarrierResponse> carriers = shipmentService.findAllActiveCarriers();
    return ResponseEntity.ok(carriers);
  }

  @GetMapping(value = "/{shipmentId}", produces = "application/json")
  public ResponseEntity<ShipmentResponse> findById(@PathVariable Long shipmentId) {
    ShipmentResponse shipment = shipmentService.findById(shipmentId);
    return ResponseEntity.ok(shipment);
  }

  @GetMapping(value = "/order/{orderId}", produces = "application/json")
  public ResponseEntity<ShipmentResponse> findByOrderId(@PathVariable Long orderId) {
    ShipmentResponse shipment = shipmentService.findByOrderId(orderId);
    return ResponseEntity.ok(shipment);
  }
}
