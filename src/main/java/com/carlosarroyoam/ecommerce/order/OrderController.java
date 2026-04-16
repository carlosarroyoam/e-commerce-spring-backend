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

@RestController
@RequestMapping("/orders")
public class OrderController {
  private final OrderService orderService;

  public OrderController(final OrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping(produces = "application/json")
  public ResponseEntity<PagedResponse<OrderResponse>> findAll(
      @Valid @ModelAttribute OrderSpecs orderSpecs,
      @PageableDefault(page = 0, size = 25, sort = "id") Pageable pageable) {
    PagedResponse<OrderResponse> orders = orderService.findAll(orderSpecs, pageable);
    return ResponseEntity.ok(orders);
  }

  @GetMapping(value = "/{orderId}", produces = "application/json")
  public ResponseEntity<OrderResponse> findById(@PathVariable Long orderId) {
    return ResponseEntity.ok(orderService.findById(orderId));
  }

  @GetMapping(value = "/track/{orderNumber}", produces = "application/json")
  public ResponseEntity<OrderTrackResponse> findByOrderNumber(@PathVariable String orderNumber) {
    return ResponseEntity.ok(orderService.findByOrderNumber(orderNumber));
  }

  @PatchMapping(value = "/{orderId}/cancel")
  public ResponseEntity<Void> cancel(@PathVariable Long orderId) {
    orderService.cancel(orderId);
    return ResponseEntity.noContent().build();
  }
}
