package com.carlosarroyoam.ecommerce.order;

import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.exception.BusinessException;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse.PagedResponseMapper;
import com.carlosarroyoam.ecommerce.core.specification.SpecificationBuilder;
import com.carlosarroyoam.ecommerce.customer.entity.Customer_;
import com.carlosarroyoam.ecommerce.order.dto.OrderResponse;
import com.carlosarroyoam.ecommerce.order.dto.OrderResponse.OrderResponseMapper;
import com.carlosarroyoam.ecommerce.order.dto.OrderSpecs;
import com.carlosarroyoam.ecommerce.order.dto.OrderTrackResponse;
import com.carlosarroyoam.ecommerce.order.dto.OrderTrackResponse.OrderTrackResponseMapper;
import com.carlosarroyoam.ecommerce.order.entity.Order;
import com.carlosarroyoam.ecommerce.order.entity.OrderStatus;
import com.carlosarroyoam.ecommerce.order.entity.OrderStatusHistory;
import com.carlosarroyoam.ecommerce.order.entity.Order_;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lógica de negocio para consultar y cancelar órdenes ({@link Order}). */
@Service
public class OrderService {
  private static final Logger log = LoggerFactory.getLogger(OrderService.class);
  private final OrderRepository orderRepository;

  public OrderService(final OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  /**
   * Obtiene una página de órdenes que cumplen los filtros indicados.
   *
   * @param orderSpecs los filtros de búsqueda
   * @param pageable la paginación y el orden a aplicar
   * @return la página de {@link OrderResponse} resultante
   */
  @Transactional(readOnly = true)
  public PagedResponse<OrderResponse> findAll(OrderSpecs orderSpecs, Pageable pageable) {
    Specification<Order> spec =
        SpecificationBuilder.<Order>builder()
            .likeIfPresent(root -> root.get(Order_.orderNumber), orderSpecs.getOrderNumber())
            .equalsIfPresent(
                root -> root.join(Order_.customer).get(Customer_.id), orderSpecs.getCustomerId())
            .equalsIfPresent(root -> root.get(Order_.status), orderSpecs.getStatus())
            .betweenDatesIfPresent(
                root -> root.get(Order_.createdAt),
                orderSpecs.getStartDate(),
                orderSpecs.getEndDate())
            .build();

    Page<Order> orders = orderRepository.findAll(spec, pageable);

    return PagedResponseMapper.INSTANCE.toPagedResponse(
        orders.map(OrderResponseMapper.INSTANCE::toDto));
  }

  /**
   * Busca una orden por su id.
   *
   * @param orderId el id de la orden
   * @return el {@link OrderResponse} correspondiente
   * @throws ResourceNotFoundException con 404 si no existe una orden con ese id
   */
  @Transactional(readOnly = true)
  public OrderResponse findById(Long orderId) {
    Order orderByIdOrFail = findOrderByIdOrFail(orderId);
    return OrderResponseMapper.INSTANCE.toDto(orderByIdOrFail);
  }

  /**
   * Busca el resumen de seguimiento público de una orden por su número de orden.
   *
   * @param orderNumber el número de la orden
   * @return el {@link OrderTrackResponse} correspondiente
   * @throws ResourceNotFoundException con 404 si no existe una orden con ese número
   */
  @Transactional(readOnly = true)
  public OrderTrackResponse findByOrderNumber(String orderNumber) {
    Order orderByOrderNumber =
        orderRepository
            .findByOrderNumber(orderNumber)
            .orElseThrow(
                () -> {
                  log.warn(AppMessages.ORDER_NOT_FOUND_EXCEPTION);
                  return new ResourceNotFoundException(AppMessages.ORDER_NOT_FOUND_EXCEPTION);
                });

    return OrderTrackResponseMapper.INSTANCE.toDto(orderByOrderNumber);
  }

  /**
   * Cancela una orden y añade una entrada al historial de estados. Solo puede cancelarse una orden
   * en estado {@code PENDING}, {@code CONFIRMED} o {@code PROCESSING}.
   *
   * @param orderId el id de la orden a cancelar
   * @throws ResourceNotFoundException con 404 si no existe una orden con ese id
   * @throws BusinessException con 422 si su estado actual no permite la cancelación
   */
  @Transactional
  public void cancel(Long orderId) {
    Order orderById = findOrderByIdOrFail(orderId);

    if (!canBeCancelled(orderById.getStatus())) {
      log.warn(AppMessages.ORDER_CANNOT_BE_CANCELLED_EXCEPTION);
      throw new BusinessException(AppMessages.ORDER_CANNOT_BE_CANCELLED_EXCEPTION);
    }

    orderById.setStatus(OrderStatus.CANCELLED);
    orderById.setUpdatedAt(LocalDateTime.now());
    orderById
        .getStatusHistory()
        .add(
            OrderStatusHistory.builder()
                .order(orderById)
                .notes("Order cancelled")
                .changedAt(LocalDateTime.now())
                .build());
    orderRepository.save(orderById);
  }

  /**
   * Indica si una orden en el estado dado puede cancelarse.
   *
   * @param orderStatus el estado actual de la orden
   * @return {@code true} si el estado es {@code PENDING}, {@code CONFIRMED} o {@code PROCESSING}
   */
  private boolean canBeCancelled(OrderStatus orderStatus) {
    return orderStatus == OrderStatus.PENDING
        || orderStatus == OrderStatus.CONFIRMED
        || orderStatus == OrderStatus.PROCESSING;
  }

  /**
   * Busca una orden por su id o lanza una excepción si no existe.
   *
   * @param orderId el id de la orden
   * @return la {@link Order} encontrada
   * @throws ResourceNotFoundException con 404 si no existe una orden con ese id
   */
  private Order findOrderByIdOrFail(Long orderId) {
    return orderRepository
        .findById(orderId)
        .orElseThrow(
            () -> {
              log.warn(AppMessages.ORDER_NOT_FOUND_EXCEPTION);
              return new ResourceNotFoundException(AppMessages.ORDER_NOT_FOUND_EXCEPTION);
            });
  }
}
