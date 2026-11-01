package com.carlosarroyoam.ecommerce.refund;

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
import com.carlosarroyoam.ecommerce.refund.dto.RefundItemResponse;
import com.carlosarroyoam.ecommerce.refund.dto.RefundResponse;
import com.carlosarroyoam.ecommerce.refund.dto.RefundSpecs;
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
class RefundControllerTest {
  @Mock private RefundService refundService;
  @InjectMocks private RefundController refundController;

  private MockMvc mockMvc;
  private RefundResponse refund;

  @BeforeEach
  void setup() {
    refund =
        RefundResponse.builder()
            .id(1L)
            .amount(new BigDecimal("8999.00"))
            .reason("Damaged merchandise")
            .items(
                List.of(
                    RefundItemResponse.builder()
                        .id(1L)
                        .quantity(1)
                        .amount(new BigDecimal("8999.00"))
                        .refundId(1L)
                        .build()))
            .orderId(1L)
            .createdAt(LocalDateTime.of(2025, 1, 20, 12, 0))
            .build();

    mockMvc =
        MockMvcBuilders.standaloneSetup(refundController)
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
  @DisplayName("GET /refunds - Given refunds exist, when find all, then returns paged refunds")
  void givenRefundsExist_whenFindAllRefunds_thenReturnsPagedRefunds() throws Exception {
    PagedResponse<RefundResponse> page =
        PagedResponse.<RefundResponse>builder()
            .items(List.of(refund))
            .pagination(
                PaginationResponse.builder().page(0).size(10).totalItems(1).totalPages(1).build())
            .build();

    when(refundService.findAll(any(RefundSpecs.class), any())).thenReturn(page);

    mockMvc
        .perform(
            get("/refunds")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].reason").value("Damaged merchandise"))
        .andExpect(jsonPath("$.items[0].order_id").value(1))
        .andExpect(jsonPath("$.items[0].items[0].refund_id").value(1))
        .andExpect(jsonPath("$.pagination.page").value(0))
        .andExpect(jsonPath("$.pagination.size").value(10))
        .andExpect(jsonPath("$.pagination.total_items").value(1))
        .andExpect(jsonPath("$.pagination.total_pages").value(1));
  }

  @Test
  @DisplayName("GET /refunds - Given order id filter, when find all, then passes filter to service")
  void givenOrderIdFilter_whenFindAllRefunds_thenPassesFilterToService() throws Exception {
    PagedResponse<RefundResponse> page =
        PagedResponse.<RefundResponse>builder()
            .items(List.of())
            .pagination(
                PaginationResponse.builder().page(0).size(10).totalItems(0).totalPages(0).build())
            .build();

    when(refundService.findAll(any(RefundSpecs.class), any())).thenReturn(page);

    mockMvc.perform(get("/refunds").queryParam("orderId", "1")).andExpect(status().isOk());

    verify(refundService)
        .findAll(
            argThat((RefundSpecs specs) -> Long.valueOf(1L).equals(specs.getOrderId())), any());
  }

  @Test
  @DisplayName("GET /refunds/{id} - Given refund exists, when find by id, then returns refund")
  void givenRefundExists_whenFindRefundById_thenReturnsRefund() throws Exception {
    when(refundService.findById(anyLong())).thenReturn(refund);

    mockMvc
        .perform(get("/refunds/{refundId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.amount").value(8999.00))
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.order_id").value(1));
  }

  @Test
  @DisplayName(
      "GET /refunds/{id} - Given refund does not exist, when find by id, then returns not found")
  void givenRefundDoesNotExist_whenFindRefundById_thenReturnsNotFound() throws Exception {
    when(refundService.findById(anyLong()))
        .thenThrow(new ResourceNotFoundException(MessageKeys.REFUND_NOT_FOUND_EXCEPTION));

    mockMvc
        .perform(get("/refunds/{refundId}", 999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Refund not found"))
        .andExpect(jsonPath("$.instance").value("/refunds/999"));
  }

  @Test
  @DisplayName(
      "GET /refunds/order/{orderId} - Given refunds exist for order, when find refunds by order id, then returns refunds")
  void givenRefundsExistForOrder_whenFindRefundsByOrderId_thenReturnsRefunds() throws Exception {
    when(refundService.findAllByOrderId(anyLong())).thenReturn(List.of(refund));

    mockMvc
        .perform(get("/refunds/order/{orderId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].order_id").value(1))
        .andExpect(jsonPath("$[0].reason").value("Damaged merchandise"));

    verify(refundService).findAllByOrderId(1L);
  }

  @Test
  @DisplayName(
      "GET /refunds/order/{orderId} - Given no refund for order, when find refunds by order id, then returns empty list")
  void givenNoRefundForOrder_whenFindRefundsByOrderId_thenReturnsEmptyList() throws Exception {
    when(refundService.findAllByOrderId(anyLong())).thenReturn(List.of());

    mockMvc
        .perform(get("/refunds/order/{orderId}", 3L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(0));
  }
}
