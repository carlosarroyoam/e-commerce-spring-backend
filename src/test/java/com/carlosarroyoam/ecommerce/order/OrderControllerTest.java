package com.carlosarroyoam.ecommerce.order;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.ecommerce.core.constant.MessageKeys;
import com.carlosarroyoam.ecommerce.core.exception.BusinessException;
import com.carlosarroyoam.ecommerce.core.exception.ExceptionLogger;
import com.carlosarroyoam.ecommerce.core.exception.GlobalExceptionHandler;
import com.carlosarroyoam.ecommerce.core.exception.ProblemDetailFactory;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.core.pagination.PaginationResponse;
import com.carlosarroyoam.ecommerce.order.dto.OrderResponse;
import com.carlosarroyoam.ecommerce.order.dto.OrderSpecs;
import com.carlosarroyoam.ecommerce.order.dto.OrderStatusHistoryResponse;
import com.carlosarroyoam.ecommerce.order.dto.OrderTrackResponse;
import com.carlosarroyoam.ecommerce.order.entity.OrderStatus;
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
class OrderControllerTest {
  private static final String ORDER_NUMBER = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

  @Mock private OrderService orderService;
  @InjectMocks private OrderController orderController;

  private MockMvc mockMvc;
  private OrderResponse order;
  private OrderTrackResponse orderTrack;

  @BeforeEach
  void setup() {
    order =
        OrderResponse.builder()
            .id(1L)
            .orderNumber(ORDER_NUMBER)
            .subtotal(new BigDecimal("8999.00"))
            .taxTotal(new BigDecimal("800.00"))
            .shippingTotal(new BigDecimal("150.00"))
            .total(new BigDecimal("9949.00"))
            .status(OrderStatus.CONFIRMED)
            .build();

    orderTrack =
        OrderTrackResponse.builder()
            .orderNumber(ORDER_NUMBER)
            .status(OrderStatus.CONFIRMED)
            .total(new BigDecimal("9949.00"))
            .shipments(List.of())
            .statusHistory(
                List.of(
                    OrderStatusHistoryResponse.builder()
                        .id(1L)
                        .notes("Order created")
                        .changedAt(LocalDateTime.of(2025, 1, 15, 10, 30))
                        .build()))
            .createdAt(LocalDateTime.of(2025, 1, 15, 10, 30))
            .build();

    mockMvc =
        MockMvcBuilders.standaloneSetup(orderController)
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
  @DisplayName("GET /orders - Given orders exist, when find all, then returns paged orders")
  void givenOrdersExist_whenFindAllOrders_thenReturnsPagedOrders() throws Exception {
    PagedResponse<OrderResponse> page =
        PagedResponse.<OrderResponse>builder()
            .items(List.of(order))
            .pagination(
                PaginationResponse.builder().page(0).size(10).totalItems(1).totalPages(1).build())
            .build();

    when(orderService.findAll(any(OrderSpecs.class), any())).thenReturn(page);

    mockMvc
        .perform(
            get("/orders")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].order_number").value(ORDER_NUMBER))
        .andExpect(jsonPath("$.items[0].shipping_total").value(150.00))
        .andExpect(jsonPath("$.items[0].status").value("CONFIRMED"))
        .andExpect(jsonPath("$.pagination.page").value(0))
        .andExpect(jsonPath("$.pagination.size").value(10))
        .andExpect(jsonPath("$.pagination.total_items").value(1))
        .andExpect(jsonPath("$.pagination.total_pages").value(1));
  }

  @Test
  @DisplayName(
      "GET /orders - Given status and customer id filters, when find all, then passes filters to service")
  void givenStatusAndCustomerIdFilters_whenFindAllOrders_thenPassesFiltersToService()
      throws Exception {
    PagedResponse<OrderResponse> page =
        PagedResponse.<OrderResponse>builder()
            .items(List.of())
            .pagination(
                PaginationResponse.builder().page(0).size(10).totalItems(0).totalPages(0).build())
            .build();

    when(orderService.findAll(any(OrderSpecs.class), any())).thenReturn(page);

    mockMvc
        .perform(get("/orders").queryParam("status", "PENDING").queryParam("customerId", "3"))
        .andExpect(status().isOk());

    verify(orderService)
        .findAll(
            argThat((OrderSpecs specs) ->
                specs.getStatus() == OrderStatus.PENDING
                    && Long.valueOf(3L).equals(specs.getCustomerId())),
            any());
  }

  @Test
  @DisplayName("GET /orders/{id} - Given order exists, when find by id, then returns order")
  void givenOrderExists_whenFindOrderById_thenReturnsOrder() throws Exception {
    when(orderService.findById(anyLong())).thenReturn(order);

    mockMvc
        .perform(get("/orders/{orderId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.order_number").value(ORDER_NUMBER))
        .andExpect(jsonPath("$.tax_total").value(800.00))
        .andExpect(jsonPath("$.status").value("CONFIRMED"));
  }

  @Test
  @DisplayName(
      "GET /orders/{id} - Given order does not exist, when find by id, then returns not found")
  void givenOrderDoesNotExist_whenFindOrderById_thenReturnsNotFound() throws Exception {
    when(orderService.findById(anyLong()))
        .thenThrow(new ResourceNotFoundException(MessageKeys.ORDER_NOT_FOUND_EXCEPTION));

    mockMvc
        .perform(get("/orders/{orderId}", 999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Order not found"))
        .andExpect(jsonPath("$.instance").value("/orders/999"));
  }

  @Test
  @DisplayName(
      "GET /orders/track/{orderNumber} - Given order exists, when find order by order number, then returns order tracking")
  void givenOrderExists_whenFindOrderByOrderNumber_thenReturnsOrderTracking() throws Exception {
    when(orderService.findByOrderNumber(anyString())).thenReturn(orderTrack);

    mockMvc
        .perform(
            get("/orders/track/{orderNumber}", ORDER_NUMBER).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.order_number").value(ORDER_NUMBER))
        .andExpect(jsonPath("$.status").value("CONFIRMED"))
        .andExpect(jsonPath("$.total").value(9949.00))
        .andExpect(jsonPath("$.status_history.length()").value(1))
        .andExpect(jsonPath("$.status_history[0].notes").value("Order created"));

    verify(orderService).findByOrderNumber(ORDER_NUMBER);
  }

  @Test
  @DisplayName(
      "GET /orders/track/{orderNumber} - Given order does not exist, when find order by order number, then returns not found")
  void givenOrderDoesNotExist_whenFindOrderByOrderNumber_thenReturnsNotFound() throws Exception {
    when(orderService.findByOrderNumber(anyString()))
        .thenThrow(new ResourceNotFoundException(MessageKeys.ORDER_NOT_FOUND_EXCEPTION));

    mockMvc
        .perform(get("/orders/track/{orderNumber}", "unknown"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Order not found"))
        .andExpect(jsonPath("$.instance").value("/orders/track/unknown"));
  }

  @Test
  @DisplayName(
      "PATCH /orders/{id}/cancel - Given order exists, when cancel, then returns no content")
  void givenOrderExists_whenCancelOrder_thenReturnsNoContent() throws Exception {
    mockMvc.perform(patch("/orders/{orderId}/cancel", 4L)).andExpect(status().isNoContent());

    verify(orderService).cancel(4L);
  }

  @Test
  @DisplayName(
      "PATCH /orders/{id}/cancel - Given order does not exist, when cancel, then returns not found")
  void givenOrderDoesNotExist_whenCancelOrder_thenReturnsNotFound() throws Exception {
    doThrow(new ResourceNotFoundException(MessageKeys.ORDER_NOT_FOUND_EXCEPTION))
        .when(orderService)
        .cancel(999L);

    mockMvc
        .perform(patch("/orders/{orderId}/cancel", 999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Order not found"))
        .andExpect(jsonPath("$.instance").value("/orders/999/cancel"));
  }

  @Test
  @DisplayName(
      "PATCH /orders/{id}/cancel - Given order cannot be cancelled, when cancel, then returns unprocessable entity")
  void givenOrderCannotBeCancelled_whenCancelOrder_thenReturnsUnprocessableEntity()
      throws Exception {
    doThrow(new BusinessException(MessageKeys.ORDER_CANNOT_BE_CANCELED_EXCEPTION))
        .when(orderService)
        .cancel(2L);

    mockMvc
        .perform(patch("/orders/{orderId}/cancel", 2L))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.detail").value("Order cannot be canceled"))
        .andExpect(jsonPath("$.instance").value("/orders/2/cancel"));
  }
}
