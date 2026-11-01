package com.carlosarroyoam.ecommerce.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.ecommerce.core.constant.MessageKeys;
import com.carlosarroyoam.ecommerce.core.exception.BusinessException;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.customer.entity.Customer;
import com.carlosarroyoam.ecommerce.customer.entity.CustomerAddress;
import com.carlosarroyoam.ecommerce.customer.entity.CustomerStatus;
import com.carlosarroyoam.ecommerce.order.dto.OrderResponse;
import com.carlosarroyoam.ecommerce.order.dto.OrderSpecs;
import com.carlosarroyoam.ecommerce.order.dto.OrderTrackResponse;
import com.carlosarroyoam.ecommerce.order.entity.Order;
import com.carlosarroyoam.ecommerce.order.entity.OrderStatus;
import com.carlosarroyoam.ecommerce.order.entity.OrderStatusHistory;
import com.carlosarroyoam.ecommerce.payment.entity.Payment;
import com.carlosarroyoam.ecommerce.payment.entity.PaymentMethod;
import com.carlosarroyoam.ecommerce.payment.entity.PaymentStatus;
import com.carlosarroyoam.ecommerce.shipment.entity.Carrier;
import com.carlosarroyoam.ecommerce.shipment.entity.Shipment;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
  private static final String ORDER_NUMBER = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

  @Mock private OrderRepository orderRepository;
  @Spy private Clock clock = Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneOffset.UTC);
  @InjectMocks private OrderService orderService;

  private Order order;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now(clock);

    Customer customer =
        Customer.builder()
            .id(1L)
            .firstName("Jose")
            .lastName("Garcia")
            .email("jose.garcia@example.com")
            .status(CustomerStatus.ACTIVE)
            .createdAt(now.minusDays(30))
            .updatedAt(now.minusDays(30))
            .build();

    CustomerAddress shippingAddress =
        CustomerAddress.builder()
            .id(1L)
            .streetName("Insurgentes Sur")
            .streetNumber("1234")
            .country("Mexico")
            .isDefault(true)
            .customer(customer)
            .build();

    order =
        Order.builder()
            .id(1L)
            .orderNumber(ORDER_NUMBER)
            .customer(customer)
            .shippingAddress(shippingAddress)
            .subtotal(new BigDecimal("8999.00"))
            .taxTotal(new BigDecimal("800.00"))
            .shippingTotal(new BigDecimal("150.00"))
            .total(new BigDecimal("9949.00"))
            .notes("Please handle with care")
            .status(OrderStatus.CONFIRMED)
            .createdAt(now.minusDays(2))
            .updatedAt(now.minusDays(2))
            .build();

    order
        .getStatusHistory()
        .add(
            OrderStatusHistory.builder()
                .id(1L)
                .order(order)
                .notes("Order created")
                .changedAt(now.minusDays(2))
                .build());
    order
        .getPayments()
        .add(
            Payment.builder()
                .id(1L)
                .amount(new BigDecimal("8999.00"))
                .reference("123456789012")
                .method(PaymentMethod.DEBIT_CARD)
                .status(PaymentStatus.COMPLETED)
                .order(order)
                .build());
    order
        .getShipments()
        .add(
            Shipment.builder()
                .id(1L)
                .trackingNumber("FX123456789012")
                .carrier(Carrier.builder().id((byte) 1).name("FedEx").build())
                .order(order)
                .build());
  }

  @Test
  @DisplayName("Given orders exist, when find all, then returns paged orders")
  void givenOrdersExist_whenFindAll_thenReturnsPagedOrders() {
    OrderSpecs orderSpecs = OrderSpecs.builder().status(OrderStatus.CONFIRMED).build();
    Pageable pageable = PageRequest.of(0, 10);
    when(orderRepository.findAll(ArgumentMatchers.<Specification<Order>>any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(order), pageable, 1));

    PagedResponse<OrderResponse> response = orderService.findAll(orderSpecs, pageable);

    assertThat(response.getItems())
        .hasSize(1)
        .first()
        .satisfies(item -> {
          assertThat(item.getOrderNumber()).isEqualTo(ORDER_NUMBER);
          assertThat(item.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
          assertThat(item.getCustomer().getEmail()).isEqualTo("jose.garcia@example.com");
          assertThat(item.getPayments()).extracting("orderId").containsExactly(1L);
          assertThat(item.getShipments())
              .extracting("trackingNumber")
              .containsExactly("FX123456789012");
          assertThat(item.getStatusHistory()).extracting("notes").containsExactly("Order created");
        });
    assertThat(response.getPagination().getPage()).isZero();
    assertThat(response.getPagination().getSize()).isEqualTo(10);
    assertThat(response.getPagination().getTotalItems()).isEqualTo(1);
    assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
  }

  @Test
  @DisplayName("Given order exists, when find by id, then returns order")
  void givenOrderExists_whenFindById_thenReturnsOrder() {
    when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

    OrderResponse response = orderService.findById(1L);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getOrderNumber()).isEqualTo(ORDER_NUMBER);
    assertThat(response.getTotal()).isEqualByComparingTo("9949.00");
    assertThat(response.getShippingAddress().getStreetName()).isEqualTo("Insurgentes Sur");
  }

  @Test
  @DisplayName("Given order does not exist, when find by id, then throws not found exception")
  void givenOrderDoesNotExist_whenFindById_thenThrowsNotFoundException() {
    when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.findById(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(MessageKeys.ORDER_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Given order exists, when find by order number, then returns order tracking")
  void givenOrderExists_whenFindByOrderNumber_thenReturnsOrderTracking() {
    when(orderRepository.findByOrderNumber(anyString())).thenReturn(Optional.of(order));

    OrderTrackResponse response = orderService.findByOrderNumber(ORDER_NUMBER);

    assertThat(response).isNotNull();
    assertThat(response.getOrderNumber()).isEqualTo(ORDER_NUMBER);
    assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    assertThat(response.getTotal()).isEqualByComparingTo("9949.00");
    assertThat(response.getShipments())
        .extracting("trackingNumber")
        .containsExactly("FX123456789012");
    assertThat(response.getStatusHistory()).extracting("notes").containsExactly("Order created");
  }

  @Test
  @DisplayName(
      "Given order does not exist, when find by order number, then throws not found exception")
  void givenOrderDoesNotExist_whenFindByOrderNumber_thenThrowsNotFoundException() {
    when(orderRepository.findByOrderNumber(anyString())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.findByOrderNumber("unknown"))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(MessageKeys.ORDER_NOT_FOUND_EXCEPTION);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = OrderStatus.class,
      names = {"PENDING", "CONFIRMED", "PROCESSING"})
  @DisplayName("Given cancellable order, when cancel, then marks order as cancelled")
  void givenCancellableOrder_whenCancel_thenMarksOrderAsCancelled(OrderStatus status) {
    order.setStatus(status);
    int historySizeBefore = order.getStatusHistory().size();
    when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

    orderService.cancel(1L);

    LocalDateTime now = LocalDateTime.now(clock);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    assertThat(order.getUpdatedAt()).isEqualTo(now);
    assertThat(order.getStatusHistory()).hasSize(historySizeBefore + 1);
    OrderStatusHistory entry = order.getStatusHistory().get(historySizeBefore);
    assertThat(entry.getNotes()).isEqualTo("Order cancelled");
    assertThat(entry.getChangedAt()).isEqualTo(now);
    assertThat(entry.getOrder()).isSameAs(order);
    verify(orderRepository, never()).save(any(Order.class));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = OrderStatus.class,
      names = {"SHIPPED", "DELIVERED", "CANCELLED", "REFUNDED"})
  @DisplayName("Given non cancellable order, when cancel, then throws business exception")
  void givenNonCancellableOrder_whenCancel_thenThrowsBusinessException(OrderStatus status) {
    order.setStatus(status);
    LocalDateTime updatedAtBefore = order.getUpdatedAt();
    when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.cancel(1L))
        .isInstanceOf(BusinessException.class)
        .hasMessage(MessageKeys.ORDER_CANNOT_BE_CANCELED_EXCEPTION);

    verify(orderRepository, never()).save(any(Order.class));
    assertThat(order.getStatus()).isEqualTo(status);
    assertThat(order.getUpdatedAt()).isEqualTo(updatedAtBefore);
    assertThat(order.getStatusHistory()).hasSize(1);
  }

  @Test
  @DisplayName("Given order does not exist, when cancel, then throws not found exception")
  void givenOrderDoesNotExist_whenCancel_thenThrowsNotFoundException() {
    when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.cancel(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(MessageKeys.ORDER_NOT_FOUND_EXCEPTION);

    verify(orderRepository, never()).save(any(Order.class));
  }
}
