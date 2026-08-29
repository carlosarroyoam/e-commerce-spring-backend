package com.carlosarroyoam.ecommerce.shipment;

import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.shipment.dto.CarrierResponse;
import com.carlosarroyoam.ecommerce.shipment.dto.ShipmentResponse;
import com.carlosarroyoam.ecommerce.shipment.dto.ShipmentSpecs;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Expone los endpoints REST de consulta de envíos y transportistas bajo {@code /shipments}. */
@RestController
@RequestMapping("/shipments")
public class ShipmentController {
  private final ShipmentService shipmentService;

  public ShipmentController(final ShipmentService shipmentService) {
    this.shipmentService = shipmentService;
  }

  /**
   * Lista los envíos de forma paginada y filtrable.
   *
   * @param shipmentSpecs los filtros de búsqueda
   * @param pageable la paginación y el orden a aplicar
   * @return la página de envíos y el estado 200 OK
   */
  @GetMapping(produces = "application/json")
  public ResponseEntity<PagedResponse<ShipmentResponse>> findAll(
      @Valid @ModelAttribute ShipmentSpecs shipmentSpecs,
      @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {
    PagedResponse<ShipmentResponse> shipments = shipmentService.findAll(shipmentSpecs, pageable);
    return ResponseEntity.ok(shipments);
  }

  /**
   * Obtiene un envío por su id.
   *
   * @param shipmentId el id del envío
   * @return el envío encontrado y el estado 200 OK
   */
  @GetMapping(value = "/{shipmentId}", produces = "application/json")
  public ResponseEntity<ShipmentResponse> findById(@PathVariable Long shipmentId) {
    ShipmentResponse shipmentById = shipmentService.findById(shipmentId);
    return ResponseEntity.ok(shipmentById);
  }

  /**
   * Obtiene el envío asociado a una orden.
   *
   * @param orderId el id de la orden
   * @return el envío encontrado y el estado 200 OK
   */
  @GetMapping(value = "/order/{orderId}", produces = "application/json")
  public ResponseEntity<ShipmentResponse> findByOrderId(@PathVariable Long orderId) {
    ShipmentResponse shipmentByOrderId = shipmentService.findByOrderId(orderId);
    return ResponseEntity.ok(shipmentByOrderId);
  }

  /**
   * Lista todos los transportistas disponibles.
   *
   * @return la lista de transportistas y el estado 200 OK
   */
  @GetMapping(value = "/carriers", produces = "application/json")
  public ResponseEntity<List<CarrierResponse>> findAllCarriers() {
    List<CarrierResponse> carriers = shipmentService.findAllActiveCarriers();
    return ResponseEntity.ok(carriers);
  }
}
