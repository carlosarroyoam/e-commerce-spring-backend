package com.carlosarroyoam.ecommerce.order;

import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse.PagedResponseMapper;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderService {
  private static final Logger log = LoggerFactory.getLogger(OrderService.class);
  private final OrderRepository orderRepository;

  public OrderService(final OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Transactional(readOnly = true)
  public PagedResponse<OrderResponse> findAll(OrderSpecs orderSpecs, Pageable pageable) {
    Specification<Order> spec = SpecificationBuilder.<Order>builder()
        .likeIfPresent(root -> root.get(Order_.orderNumber), orderSpecs.getOrderNumber())
        .equalsIfPresent(root -> root.join(Order_.customer).get(Customer_.id),
            orderSpecs.getCustomerId())
        .equalsIfPresent(root -> root.get(Order_.status), orderSpecs.getStatus())
        .betweenDatesIfPresent(root -> root.get(Order_.createdAt), orderSpecs.getStartDate(),
            orderSpecs.getEndDate())
        .build();

    Page<Order> orders = orderRepository.findAll(spec, pageable);

    return PagedResponseMapper.INSTANCE
        .toPagedResponse(orders.map(OrderResponseMapper.INSTANCE::toDto));
  }

  @Transactional(readOnly = true)
  public OrderResponse findById(Long orderId) {
    Order orderByIdOrFail = findOrderByIdOrFail(orderId);
    return OrderResponseMapper.INSTANCE.toDto(orderByIdOrFail);
  }

  @Transactional(readOnly = true)
  public OrderTrackResponse findByOrderNumber(String orderNumber) {
    Order orderByOrderNumber = orderRepository.findByOrderNumber(orderNumber).orElseThrow(() -> {
      log.warn(AppMessages.ORDER_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.ORDER_NOT_FOUND_EXCEPTION);
    });

    return OrderTrackResponseMapper.INSTANCE.toDto(orderByOrderNumber);
  }

  @Transactional
  public void cancel(Long orderId) {
    Order orderById = findOrderByIdOrFail(orderId);

    if (!canBeCancelled(orderById.getStatus())) {
      log.warn(AppMessages.ORDER_CANNOT_BE_CANCELLED_EXCEPTION);
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
          AppMessages.ORDER_CANNOT_BE_CANCELLED_EXCEPTION);
    }

    orderById.setStatus(OrderStatus.CANCELLED);
    orderById.setUpdatedAt(LocalDateTime.now());
    orderById.getStatusHistory()
        .add(OrderStatusHistory.builder()
            .order(orderById)
            .notes("Order cancelled")
            .changedAt(LocalDateTime.now())
            .build());
    orderRepository.save(orderById);
  }

  private boolean canBeCancelled(OrderStatus orderStatus) {
    return orderStatus == OrderStatus.PENDING || orderStatus == OrderStatus.CONFIRMED
        || orderStatus == OrderStatus.PROCESSING;
  }

  private Order findOrderByIdOrFail(Long orderId) {
    return orderRepository.findById(orderId).orElseThrow(() -> {
      log.warn(AppMessages.ORDER_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.ORDER_NOT_FOUND_EXCEPTION);
    });
  }
}
