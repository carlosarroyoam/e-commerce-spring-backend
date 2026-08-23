package com.carlosarroyoam.ecommerce.payment;

import com.carlosarroyoam.ecommerce.core.dto.PagedResponse;
import com.carlosarroyoam.ecommerce.payment.dto.PaymentResponse;
import com.carlosarroyoam.ecommerce.payment.dto.PaymentSpecs;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Expone los endpoints REST de consulta de pagos bajo {@code /payments}. */
@RestController
@RequestMapping("/payments")
public class PaymentController {
  private final PaymentService paymentService;

  public PaymentController(final PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  /**
   * Lista los pagos de forma paginada y filtrable.
   *
   * @param paymentSpecs los filtros de búsqueda
   * @param pageable la paginación y el orden a aplicar
   * @return la página de pagos y el estado 200 OK
   */
  @GetMapping(produces = "application/json")
  public ResponseEntity<PagedResponse<PaymentResponse>> findAll(
      @Valid @ModelAttribute PaymentSpecs paymentSpecs,
      @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {
    PagedResponse<PaymentResponse> payments = paymentService.findAll(paymentSpecs, pageable);
    return ResponseEntity.ok(payments);
  }

  /**
   * Obtiene un pago por su id.
   *
   * @param paymentId el id del pago
   * @return el pago encontrado y el estado 200 OK
   */
  @GetMapping(value = "/{paymentId}", produces = "application/json")
  public ResponseEntity<PaymentResponse> findById(@PathVariable Long paymentId) {
    PaymentResponse paymentById = paymentService.findById(paymentId);
    return ResponseEntity.ok(paymentById);
  }
}
