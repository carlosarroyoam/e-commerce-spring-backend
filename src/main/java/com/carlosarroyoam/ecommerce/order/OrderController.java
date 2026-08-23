package com.carlosarroyoam.ecommerce.order;

import com.carlosarroyoam.ecommerce.core.dto.PagedResponse;
import com.carlosarroyoam.ecommerce.order.dto.OrderResponse;
import com.carlosarroyoam.ecommerce.order.dto.OrderSpecs;
import com.carlosarroyoam.ecommerce.order.dto.OrderTrackResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Expone los endpoints REST de consulta y cancelación de órdenes bajo {@code /orders}. */
@RestController
@RequestMapping("/orders")
public class OrderController {
  private final OrderService orderService;

  public OrderController(final OrderService orderService) {
    this.orderService = orderService;
  }

  /**
   * Lista las órdenes de forma paginada y filtrable.
   *
   * @param orderSpecs los filtros de búsqueda
   * @param pageable la paginación y el orden a aplicar
   * @return la página de órdenes y el estado 200 OK
   */
  @GetMapping(produces = "application/json")
  public ResponseEntity<PagedResponse<OrderResponse>> findAll(
      @Valid @ModelAttribute OrderSpecs orderSpecs,
      @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {
    PagedResponse<OrderResponse> orders = orderService.findAll(orderSpecs, pageable);
    return ResponseEntity.ok(orders);
  }

  /**
   * Obtiene una orden por su id.
   *
   * @param orderId el id de la orden
   * @return la orden encontrada y el estado 200 OK
   */
  @GetMapping(value = "/{orderId}", produces = "application/json")
  public ResponseEntity<OrderResponse> findById(@PathVariable Long orderId) {
    OrderResponse orderById = orderService.findById(orderId);
    return ResponseEntity.ok(orderById);
  }

  /**
   * Obtiene el resumen de seguimiento público de una orden por su número de orden.
   *
   * @param orderNumber el número de la orden
   * @return el resumen de seguimiento y el estado 200 OK
   */
  @GetMapping(value = "/track/{orderNumber}", produces = "application/json")
  public ResponseEntity<OrderTrackResponse> findByOrderNumber(@PathVariable String orderNumber) {
    OrderTrackResponse orderByOrderNumber = orderService.findByOrderNumber(orderNumber);
    return ResponseEntity.ok(orderByOrderNumber);
  }

  /**
   * Cancela una orden, si su estado actual lo permite.
   *
   * @param orderId el id de la orden a cancelar
   * @return estado 204 No Content
   */
  @PatchMapping(value = "/{orderId}/cancel")
  public ResponseEntity<Void> cancel(@PathVariable Long orderId) {
    orderService.cancel(orderId);
    return ResponseEntity.noContent().build();
  }
}
