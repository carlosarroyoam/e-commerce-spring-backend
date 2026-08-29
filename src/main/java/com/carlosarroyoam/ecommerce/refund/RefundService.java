package com.carlosarroyoam.ecommerce.refund;

import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse.PagedResponseMapper;
import com.carlosarroyoam.ecommerce.core.specification.SpecificationBuilder;
import com.carlosarroyoam.ecommerce.order.entity.Order_;
import com.carlosarroyoam.ecommerce.refund.dto.RefundResponse;
import com.carlosarroyoam.ecommerce.refund.dto.RefundResponse.RefundResponseMapper;
import com.carlosarroyoam.ecommerce.refund.dto.RefundSpecs;
import com.carlosarroyoam.ecommerce.refund.entity.Refund;
import com.carlosarroyoam.ecommerce.refund.entity.Refund_;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lógica de negocio para consultar reembolsos ({@link Refund}). */
@Service
public class RefundService {
  private final RefundRepository refundRepository;

  public RefundService(final RefundRepository refundRepository) {
    this.refundRepository = refundRepository;
  }

  /**
   * Obtiene una página de reembolsos que cumplen los filtros indicados.
   *
   * @param refundSpecs los filtros de búsqueda
   * @param pageable la paginación y el orden a aplicar
   * @return la página de {@link RefundResponse} resultante
   */
  @Transactional(readOnly = true)
  public PagedResponse<RefundResponse> findAll(RefundSpecs refundSpecs, Pageable pageable) {
    Specification<Refund> spec =
        SpecificationBuilder.<Refund>builder()
            .equalsIfPresent(
                root -> root.join(Refund_.order).get(Order_.id), refundSpecs.getOrderId())
            .build();

    Page<Refund> refunds = refundRepository.findAll(spec, pageable);

    return PagedResponseMapper.INSTANCE.toPagedResponse(
        refunds.map(RefundResponseMapper.INSTANCE::toDto));
  }

  /**
   * Busca un reembolso por su id.
   *
   * @param refundId el id del reembolso
   * @return el {@link RefundResponse} correspondiente
   * @throws ResourceNotFoundException con 404 si no existe un reembolso con ese id
   */
  @Transactional(readOnly = true)
  public RefundResponse findById(Long refundId) {
    Refund refundById = findRefundByIdOrFail(refundId);
    return RefundResponseMapper.INSTANCE.toDto(refundById);
  }

  /**
   * Busca el reembolso asociado a una orden.
   *
   * @param orderId el id de la orden
   * @return el {@link RefundResponse} correspondiente
   * @throws ResourceNotFoundException con 404 si la orden no tiene un reembolso asociado
   */
  @Transactional(readOnly = true)
  public RefundResponse findByOrderId(Long orderId) {
    Refund refundByOrderId = findRefundByOrderIdOrFail(orderId);
    return RefundResponseMapper.INSTANCE.toDto(refundByOrderId);
  }

  /**
   * Busca un reembolso por su id o lanza una excepción si no existe.
   *
   * @param refundId el id del reembolso
   * @return el {@link Refund} encontrado
   * @throws ResourceNotFoundException con 404 si no existe un reembolso con ese id
   */
  private Refund findRefundByIdOrFail(Long refundId) {
    return refundRepository
        .findById(refundId)
        .orElseThrow(() -> new ResourceNotFoundException(AppMessages.REFUND_NOT_FOUND_EXCEPTION));
  }

  /**
   * Busca el reembolso asociado a una orden o lanza una excepción si no existe.
   *
   * @param orderId el id de la orden
   * @return el {@link Refund} encontrado
   * @throws ResourceNotFoundException con 404 si la orden no tiene un reembolso asociado
   */
  private Refund findRefundByOrderIdOrFail(Long orderId) {
    return refundRepository
        .findByOrderId(orderId)
        .orElseThrow(() -> new ResourceNotFoundException(AppMessages.REFUND_NOT_FOUND_EXCEPTION));
  }
}
