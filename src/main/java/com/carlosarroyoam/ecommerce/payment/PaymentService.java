package com.carlosarroyoam.ecommerce.payment;

import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse.PagedResponseMapper;
import com.carlosarroyoam.ecommerce.core.specification.SpecificationBuilder;
import com.carlosarroyoam.ecommerce.payment.dto.PaymentResponse;
import com.carlosarroyoam.ecommerce.payment.dto.PaymentResponse.PaymentResponseMapper;
import com.carlosarroyoam.ecommerce.payment.dto.PaymentSpecs;
import com.carlosarroyoam.ecommerce.payment.entity.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Lógica de negocio para consultar pagos ({@link Payment}). */
@Service
public class PaymentService {
  private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
  private final PaymentRepository paymentRepository;

  public PaymentService(final PaymentRepository paymentRepository) {
    this.paymentRepository = paymentRepository;
  }

  /**
   * Obtiene una página de pagos que cumplen los filtros indicados.
   *
   * @param paymentSpecs los filtros de búsqueda
   * @param pageable la paginación y el orden a aplicar
   * @return la página de {@link PaymentResponse} resultante
   */
  @Transactional(readOnly = true)
  public PagedResponse<PaymentResponse> findAll(PaymentSpecs paymentSpecs, Pageable pageable) {
    Specification<Payment> spec =
        SpecificationBuilder.<Payment>builder()
            .equalsIfPresent(root -> root.join("order").get("id"), paymentSpecs.getOrderId())
            .likeIfPresent(root -> root.get("reference"), paymentSpecs.getReference())
            .equalsIfPresent(root -> root.get("method"), paymentSpecs.getMethod())
            .equalsIfPresent(root -> root.get("status"), paymentSpecs.getStatus())
            .betweenDatesIfPresent(
                root -> root.get("createdAt"),
                paymentSpecs.getStartDate(),
                paymentSpecs.getEndDate())
            .build();

    Page<Payment> payments = paymentRepository.findAll(spec, pageable);

    return PagedResponseMapper.INSTANCE.toPagedResponse(
        payments.map(PaymentResponseMapper.INSTANCE::toDto));
  }

  /**
   * Busca un pago por su id.
   *
   * @param paymentId el id del pago
   * @return el {@link PaymentResponse} correspondiente
   * @throws ResponseStatusException con 404 si no existe un pago con ese id
   */
  @Transactional(readOnly = true)
  public PaymentResponse findById(Long paymentId) {
    Payment paymentById = findPaymentByIdOrFail(paymentId);
    return PaymentResponseMapper.INSTANCE.toDto(paymentById);
  }

  /**
   * Busca un pago por su id o lanza una excepción si no existe.
   *
   * @param paymentId el id del pago
   * @return el {@link Payment} encontrado
   * @throws ResponseStatusException con 404 si no existe un pago con ese id
   */
  private Payment findPaymentByIdOrFail(Long paymentId) {
    return paymentRepository
        .findById(paymentId)
        .orElseThrow(
            () -> {
              log.warn(AppMessages.PAYMENT_NOT_FOUND_EXCEPTION);
              return new ResponseStatusException(
                  HttpStatus.NOT_FOUND, AppMessages.PAYMENT_NOT_FOUND_EXCEPTION);
            });
  }
}
