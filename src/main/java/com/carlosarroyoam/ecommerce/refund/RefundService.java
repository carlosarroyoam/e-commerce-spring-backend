package com.carlosarroyoam.ecommerce.refund;

import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse.PagedResponseMapper;
import com.carlosarroyoam.ecommerce.core.specification.SpecificationBuilder;
import com.carlosarroyoam.ecommerce.order.entity.Order_;
import com.carlosarroyoam.ecommerce.refund.dto.RefundResponse;
import com.carlosarroyoam.ecommerce.refund.dto.RefundResponse.RefundResponseMapper;
import com.carlosarroyoam.ecommerce.refund.dto.RefundSpecs;
import com.carlosarroyoam.ecommerce.refund.entity.Refund;
import com.carlosarroyoam.ecommerce.refund.entity.Refund_;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RefundService {
  private static final Logger log = LoggerFactory.getLogger(RefundService.class);
  private final RefundRepository refundRepository;

  public RefundService(final RefundRepository refundRepository) {
    this.refundRepository = refundRepository;
  }

  @Transactional(readOnly = true)
  public PagedResponse<RefundResponse> findAll(RefundSpecs refundSpecs, Pageable pageable) {
    Specification<Refund> spec = SpecificationBuilder.<Refund>builder()
        .equalsIfPresent(root -> root.join(Refund_.order).get(Order_.id), refundSpecs.getOrderId())
        .build();

    Page<Refund> refunds = refundRepository.findAll(spec, pageable);

    return PagedResponseMapper.INSTANCE
        .toPagedResponse(refunds.map(RefundResponseMapper.INSTANCE::toDto));
  }

  @Transactional(readOnly = true)
  public RefundResponse findById(Long refundId) {
    Refund refundById = findRefundByIdOrFail(refundId);
    return RefundResponseMapper.INSTANCE.toDto(refundById);
  }

  @Transactional(readOnly = true)
  public RefundResponse findByOrderId(Long orderId) {
    Refund refund = refundRepository.findByOrderId(orderId).orElseThrow(() -> {
      log.warn(AppMessages.REFUND_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.REFUND_NOT_FOUND_EXCEPTION);
    });

    return RefundResponseMapper.INSTANCE.toDto(refund);
  }

  private Refund findRefundByIdOrFail(Long refundId) {
    return refundRepository.findById(refundId).orElseThrow(() -> {
      log.warn(AppMessages.REFUND_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.REFUND_NOT_FOUND_EXCEPTION);
    });
  }
}
