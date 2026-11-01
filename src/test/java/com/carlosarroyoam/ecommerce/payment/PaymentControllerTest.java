package com.carlosarroyoam.ecommerce.payment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.ecommerce.core.constant.MessageKeys;
import com.carlosarroyoam.ecommerce.core.exception.ExceptionLogger;
import com.carlosarroyoam.ecommerce.core.exception.GlobalExceptionHandler;
import com.carlosarroyoam.ecommerce.core.exception.ProblemDetailFactory;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.core.pagination.PaginationResponse;
import com.carlosarroyoam.ecommerce.payment.dto.PaymentResponse;
import com.carlosarroyoam.ecommerce.payment.dto.PaymentSpecs;
import com.carlosarroyoam.ecommerce.payment.entity.PaymentMethod;
import com.carlosarroyoam.ecommerce.payment.entity.PaymentStatus;
import com.carlosarroyoam.ecommerce.support.testutils.TestMessageSources;
import com.carlosarroyoam.ecommerce.support.testutils.TestObjectMappers;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {
  @Mock private PaymentService paymentService;
  @InjectMocks private PaymentController paymentController;

  private MockMvc mockMvc;
  private PaymentResponse payment;

  @BeforeEach
  void setup() {
    payment =
        PaymentResponse.builder()
            .id(1L)
            .amount(new BigDecimal("8999.00"))
            .reference("123456789012")
            .description("Order payed")
            .method(PaymentMethod.DEBIT_CARD)
            .status(PaymentStatus.COMPLETED)
            .orderId(1L)
            .createdAt(LocalDateTime.of(2025, 1, 15, 10, 30))
            .updatedAt(LocalDateTime.of(2025, 1, 15, 10, 30))
            .build();

    mockMvc =
        MockMvcBuilders.standaloneSetup(paymentController)
            .setControllerAdvice(
                new GlobalExceptionHandler(
                    new ProblemDetailFactory(),
                    new ExceptionLogger(),
                    TestMessageSources.messages()))
            .setMessageConverters(
                new MappingJackson2HttpMessageConverter(TestObjectMappers.snakeCase()))
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
  }

  @Test
  @DisplayName("GET /payments - Given payments exist, when find all, then returns paged payments")
  void givenPaymentsExist_whenFindAllPayments_thenReturnsPagedPayments() throws Exception {
    PagedResponse<PaymentResponse> page =
        PagedResponse.<PaymentResponse>builder()
            .items(List.of(payment))
            .pagination(
                PaginationResponse.builder().page(0).size(10).totalItems(1).totalPages(1).build())
            .build();

    when(paymentService.findAll(any(PaymentSpecs.class), any())).thenReturn(page);

    mockMvc
        .perform(
            get("/payments")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].reference").value("123456789012"))
        .andExpect(jsonPath("$.items[0].order_id").value(1))
        .andExpect(jsonPath("$.items[0].method").value("DEBIT_CARD"))
        .andExpect(jsonPath("$.items[0].status").value("COMPLETED"))
        .andExpect(jsonPath("$.pagination.page").value(0))
        .andExpect(jsonPath("$.pagination.size").value(10))
        .andExpect(jsonPath("$.pagination.total_items").value(1))
        .andExpect(jsonPath("$.pagination.total_pages").value(1));
  }

  @Test
  @DisplayName("GET /payments - Given filters, when find all, then passes filters to service")
  void givenFilters_whenFindAllPayments_thenPassesFiltersToService() throws Exception {
    PagedResponse<PaymentResponse> page =
        PagedResponse.<PaymentResponse>builder()
            .items(List.of())
            .pagination(
                PaginationResponse.builder().page(0).size(10).totalItems(0).totalPages(0).build())
            .build();

    when(paymentService.findAll(any(PaymentSpecs.class), any())).thenReturn(page);

    mockMvc
        .perform(
            get("/payments")
                .queryParam("reference", "1234")
                .queryParam("method", "CREDIT_CARD")
                .queryParam("status", "PENDING")
                .queryParam("orderId", "2"))
        .andExpect(status().isOk());

    verify(paymentService)
        .findAll(
            argThat((PaymentSpecs specs) ->
                "1234".equals(specs.getReference())
                    && specs.getMethod() == PaymentMethod.CREDIT_CARD
                    && specs.getStatus() == PaymentStatus.PENDING
                    && Long.valueOf(2L).equals(specs.getOrderId())),
            any());
  }

  @Test
  @DisplayName("GET /payments/{id} - Given payment exists, when find by id, then returns payment")
  void givenPaymentExists_whenFindPaymentById_thenReturnsPayment() throws Exception {
    when(paymentService.findById(anyLong())).thenReturn(payment);

    mockMvc
        .perform(get("/payments/{paymentId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.amount").value(8999.00))
        .andExpect(jsonPath("$.description").value("Order payed"))
        .andExpect(jsonPath("$.order_id").value(1));
  }

  @Test
  @DisplayName(
      "GET /payments/{id} - Given payment does not exist, when find by id, then returns not found")
  void givenPaymentDoesNotExist_whenFindPaymentById_thenReturnsNotFound() throws Exception {
    when(paymentService.findById(anyLong()))
        .thenThrow(new ResourceNotFoundException(MessageKeys.PAYMENT_NOT_FOUND_EXCEPTION));

    mockMvc
        .perform(get("/payments/{paymentId}", 999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Payment not found"))
        .andExpect(jsonPath("$.instance").value("/payments/999"));
  }
}
