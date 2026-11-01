package com.carlosarroyoam.ecommerce.refund;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.ecommerce.core.constant.MessageKeys;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.order.entity.Order;
import com.carlosarroyoam.ecommerce.order.entity.OrderItem;
import com.carlosarroyoam.ecommerce.refund.dto.RefundResponse;
import com.carlosarroyoam.ecommerce.refund.dto.RefundSpecs;
import com.carlosarroyoam.ecommerce.refund.entity.Refund;
import com.carlosarroyoam.ecommerce.refund.entity.RefundItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {
  @Mock private RefundRepository refundRepository;
  @InjectMocks private RefundService refundService;

  private Refund refund;

  @BeforeEach
  void setUp() {
    Order order = Order.builder().id(1L).build();

    refund =
        Refund.builder()
            .id(1L)
            .amount(new BigDecimal("8999.00"))
            .reason("Damaged merchandise")
            .order(order)
            .createdAt(LocalDateTime.of(2025, 1, 20, 12, 0))
            .build();
    refund
        .getItems()
        .add(
            RefundItem.builder()
                .id(1L)
                .quantity(1)
                .amount(new BigDecimal("8999.00"))
                .refund(refund)
                .orderItem(
                    OrderItem.builder()
                        .id(1L)
                        .quantity(1)
                        .unitPrice(new BigDecimal("8999.00"))
                        .total(new BigDecimal("8999.00"))
                        .order(order)
                        .build())
                .build());
  }

  @Test
  @DisplayName("Given refunds exist, when find all, then returns paged refunds")
  void givenRefundsExist_whenFindAll_thenReturnsPagedRefunds() {
    RefundSpecs refundSpecs = RefundSpecs.builder().orderId(1L).build();
    Pageable pageable = PageRequest.of(0, 10);
    when(refundRepository.findAll(
            ArgumentMatchers.<Specification<Refund>>any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(refund), pageable, 1));

    PagedResponse<RefundResponse> response = refundService.findAll(refundSpecs, pageable);

    assertThat(response.getItems())
        .hasSize(1)
        .first()
        .satisfies(item -> {
          assertThat(item.getId()).isEqualTo(1L);
          assertThat(item.getAmount()).isEqualByComparingTo("8999.00");
          assertThat(item.getReason()).isEqualTo("Damaged merchandise");
          assertThat(item.getOrderId()).isEqualTo(1L);
          assertThat(item.getItems()).hasSize(1);
        });
    assertThat(response.getPagination().getPage()).isZero();
    assertThat(response.getPagination().getSize()).isEqualTo(10);
    assertThat(response.getPagination().getTotalItems()).isEqualTo(1);
    assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
  }

  @Test
  @DisplayName("Given no refunds, when find all, then returns empty page")
  void givenNoRefunds_whenFindAll_thenReturnsEmptyPage() {
    Pageable pageable = PageRequest.of(0, 10);
    when(refundRepository.findAll(
            ArgumentMatchers.<Specification<Refund>>any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(), pageable, 0));

    PagedResponse<RefundResponse> response =
        refundService.findAll(RefundSpecs.builder().build(), pageable);

    assertThat(response.getItems()).isEmpty();
    assertThat(response.getPagination().getTotalItems()).isZero();
    assertThat(response.getPagination().getTotalPages()).isZero();
  }

  @Test
  @DisplayName("Given refund exists, when find by id, then returns refund")
  void givenRefundExists_whenFindById_thenReturnsRefund() {
    when(refundRepository.findById(anyLong())).thenReturn(Optional.of(refund));

    RefundResponse response = refundService.findById(1L);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getOrderId()).isEqualTo(1L);
    assertThat(response.getItems()).hasSize(1);
    assertThat(response.getItems().get(0).getRefundId()).isEqualTo(1L);
    assertThat(response.getItems().get(0).getOrderItem().getOrderId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Given refund does not exist, when find by id, then throws not found exception")
  void givenRefundDoesNotExist_whenFindById_thenThrowsNotFoundException() {
    when(refundRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> refundService.findById(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(MessageKeys.REFUND_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Given refunds exist for order, when find all by order id, then returns refunds")
  void givenRefundsExistForOrder_whenFindAllByOrderId_thenReturnsRefunds() {
    Refund secondRefund =
        Refund.builder()
            .id(2L)
            .amount(new BigDecimal("100.00"))
            .reason("Late delivery")
            .order(refund.getOrder())
            .createdAt(LocalDateTime.of(2025, 1, 21, 12, 0))
            .build();
    when(refundRepository.findAllByOrderId(anyLong())).thenReturn(List.of(refund, secondRefund));

    List<RefundResponse> response = refundService.findAllByOrderId(1L);

    assertThat(response).hasSize(2);
    assertThat(response).extracting("id").containsExactly(1L, 2L);
    assertThat(response).extracting("orderId").containsOnly(1L);
    assertThat(response.get(0).getReason()).isEqualTo("Damaged merchandise");
  }

  @Test
  @DisplayName("Given no refund for order, when find all by order id, then returns empty list")
  void givenNoRefundForOrder_whenFindAllByOrderId_thenReturnsEmptyList() {
    when(refundRepository.findAllByOrderId(anyLong())).thenReturn(List.of());

    List<RefundResponse> response = refundService.findAllByOrderId(999L);

    assertThat(response).isEmpty();
  }
}
