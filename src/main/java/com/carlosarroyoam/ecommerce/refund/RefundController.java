package com.carlosarroyoam.ecommerce.refund;

import com.carlosarroyoam.ecommerce.core.dto.PagedResponse;
import com.carlosarroyoam.ecommerce.refund.dto.RefundResponse;
import com.carlosarroyoam.ecommerce.refund.dto.RefundSpecs;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/refunds")
public class RefundController {
  private final RefundService refundService;

  public RefundController(final RefundService refundService) {
    this.refundService = refundService;
  }

  @GetMapping(produces = "application/json")
  public ResponseEntity<PagedResponse<RefundResponse>> findAll(
      @Valid @ModelAttribute RefundSpecs refundSpecs,
      @PageableDefault(page = 0, size = 25, sort = "id") Pageable pageable) {
    PagedResponse<RefundResponse> refunds = refundService.findAll(refundSpecs, pageable);
    return ResponseEntity.ok(refunds);
  }

  @GetMapping(value = "/{refundId}", produces = "application/json")
  public ResponseEntity<RefundResponse> findById(@PathVariable Long refundId) {
    RefundResponse refundById = refundService.findById(refundId);
    return ResponseEntity.ok(refundById);
  }

  @GetMapping(value = "/order/{orderId}", produces = "application/json")
  public ResponseEntity<RefundResponse> findByOrderId(@PathVariable Long orderId) {
    RefundResponse refundByOrderId = refundService.findByOrderId(orderId);
    return ResponseEntity.ok(refundByOrderId);
  }
}
