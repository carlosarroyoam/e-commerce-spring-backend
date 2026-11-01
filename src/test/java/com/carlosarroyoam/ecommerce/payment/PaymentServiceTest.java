package com.carlosarroyoam.ecommerce.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.ecommerce.core.constant.MessageKeys;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.order.entity.Order;
import com.carlosarroyoam.ecommerce.payment.dto.PaymentResponse;
import com.carlosarroyoam.ecommerce.payment.dto.PaymentSpecs;
import com.carlosarroyoam.ecommerce.payment.entity.Payment;
import com.carlosarroyoam.ecommerce.payment.entity.PaymentMethod;
import com.carlosarroyoam.ecommerce.payment.entity.PaymentStatus;
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
class PaymentServiceTest {
  @Mock private PaymentRepository paymentRepository;
  @InjectMocks private PaymentService paymentService;

  private Payment payment;

  @BeforeEach
  void setUp() {
    LocalDateTime createdAt = LocalDateTime.of(2025, 1, 15, 10, 30);

    payment =
        Payment.builder()
            .id(1L)
            .amount(new BigDecimal("8999.00"))
            .reference("123456789012")
            .description("Order payed")
            .method(PaymentMethod.DEBIT_CARD)
            .status(PaymentStatus.COMPLETED)
            .order(Order.builder().id(1L).build())
            .createdAt(createdAt)
            .updatedAt(createdAt)
            .build();
  }

  @Test
  @DisplayName("Given payments exist, when find all, then returns paged payments")
  void givenPaymentsExist_whenFindAll_thenReturnsPagedPayments() {
    PaymentSpecs paymentSpecs =
        PaymentSpecs.builder()
            .reference("1234")
            .method(PaymentMethod.DEBIT_CARD)
            .status(PaymentStatus.COMPLETED)
            .orderId(1L)
            .build();
    Pageable pageable = PageRequest.of(0, 10);
    when(paymentRepository.findAll(
            ArgumentMatchers.<Specification<Payment>>any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(payment), pageable, 1));

    PagedResponse<PaymentResponse> response = paymentService.findAll(paymentSpecs, pageable);

    assertThat(response.getItems())
        .hasSize(1)
        .first()
        .satisfies(item -> {
          assertThat(item.getId()).isEqualTo(1L);
          assertThat(item.getAmount()).isEqualByComparingTo("8999.00");
          assertThat(item.getReference()).isEqualTo("123456789012");
          assertThat(item.getMethod()).isEqualTo(PaymentMethod.DEBIT_CARD);
          assertThat(item.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
          assertThat(item.getOrderId()).isEqualTo(1L);
        });
    assertThat(response.getPagination().getPage()).isZero();
    assertThat(response.getPagination().getSize()).isEqualTo(10);
    assertThat(response.getPagination().getTotalItems()).isEqualTo(1);
    assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
  }

  @Test
  @DisplayName("Given payment exists, when find by id, then returns payment")
  void givenPaymentExists_whenFindById_thenReturnsPayment() {
    when(paymentRepository.findById(anyLong())).thenReturn(Optional.of(payment));

    PaymentResponse response = paymentService.findById(1L);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getDescription()).isEqualTo("Order payed");
    assertThat(response.getMethod()).isEqualTo(PaymentMethod.DEBIT_CARD);
    assertThat(response.getOrderId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Given payment does not exist, when find by id, then throws not found exception")
  void givenPaymentDoesNotExist_whenFindById_thenThrowsNotFoundException() {
    when(paymentRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentService.findById(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(MessageKeys.PAYMENT_NOT_FOUND_EXCEPTION);
  }
}
