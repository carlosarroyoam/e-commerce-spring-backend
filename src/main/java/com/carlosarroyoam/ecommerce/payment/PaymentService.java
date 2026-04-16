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
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentService {
  private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
  private final PaymentRepository paymentRepository;

  public PaymentService(final PaymentRepository paymentRepository) {
    this.paymentRepository = paymentRepository;
  }

  public PagedResponse<PaymentResponse> findAll(PaymentSpecs paymentSpecs, Pageable pageable) {
    Specification<Payment> spec = SpecificationBuilder.<Payment>builder()
        .equalsIfPresent(root -> root.join("order").get("id"), paymentSpecs.getOrderId())
        .likeIfPresent(root -> root.get("reference"), paymentSpecs.getReference())
        .equalsIfPresent(root -> root.get("method"), paymentSpecs.getMethod())
        .equalsIfPresent(root -> root.get("status"), paymentSpecs.getStatus())
        .betweenDatesIfPresent(root -> root.get("createdAt"), paymentSpecs.getStartDate(),
            paymentSpecs.getEndDate())
        .build();

    Page<Payment> payments = paymentRepository.findAll(spec, pageable);

    return PagedResponseMapper.INSTANCE
        .toPagedResponse(payments.map(PaymentResponseMapper.INSTANCE::toDto));
  }

  public PaymentResponse findById(Long paymentId) {
    Payment paymentById = findPaymentEntityById(paymentId);
    return PaymentResponseMapper.INSTANCE.toDto(paymentById);
  }

  private Payment findPaymentEntityById(Long paymentId) {
    return paymentRepository.findById(paymentId).orElseThrow(() -> {
      log.warn(AppMessages.PAYMENT_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.PAYMENT_NOT_FOUND_EXCEPTION);
    });
  }
}
