package com.carlosarroyoam.ecommerce.order;

import static com.carlosarroyoam.ecommerce.support.security.AuthRequestPostProcessors.withoutAuthentication;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.ecommerce.support.AbstractIntegrationTest;
import com.carlosarroyoam.ecommerce.support.security.JwtTestTokenFactory;
import com.carlosarroyoam.ecommerce.support.testutils.JsonUtils;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderControllerIT extends AbstractIntegrationTest {
  private static final long ADMIN_ID = 1L;
  private static final String ADMIN_EMAIL = "carlos.arroyo@e-commerce.com";
  private static final String ORDER_NUMBER = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private JwtTestTokenFactory jwtTestTokenFactory;
  @Autowired private EntityManager entityManager;

  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    String adminToken = jwtTestTokenFactory.staffToken(ADMIN_ID, ADMIN_EMAIL, List.of("ADMIN"));

    mockMvc =
        MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .defaultRequest(get("/").header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
            .build();
  }

  @Test
  @DisplayName("GET /orders - Given orders exist, when find all, then returns paged orders")
  void givenOrdersExist_whenFindAllOrders_thenReturnsPagedOrders() throws Exception {
    String expectedJson = JsonUtils.readJson("orders/find-all.json");

    String responseJson =
        mockMvc
            .perform(get("/orders").queryParam("size", "2"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("GET /orders - Given status filter, when find all, then returns orders with status")
  void givenStatusFilter_whenFindAllOrders_thenReturnsOrdersWithStatus() throws Exception {
    mockMvc
        .perform(get("/orders").queryParam("status", "SHIPPED"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].id").value(2))
        .andExpect(jsonPath("$.items[0].status").value("SHIPPED"))
        .andExpect(jsonPath("$.pagination.total_items").value(1));
  }

  @Test
  @DisplayName(
      "GET /orders - Given customer id filter, when find all, then returns orders of customer")
  void givenCustomerIdFilter_whenFindAllOrders_thenReturnsOrdersOfCustomer() throws Exception {
    mockMvc
        .perform(get("/orders").queryParam("customerId", "1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.items.length()").value(2))
        .andExpect(jsonPath("$.items[0].id").value(1))
        .andExpect(jsonPath("$.items[1].id").value(2))
        .andExpect(jsonPath("$.pagination.total_items").value(2));
  }

  @Test
  @DisplayName("GET /orders - Given no authentication, when find all, then returns unauthorized")
  void givenNoAuthentication_whenFindAllOrders_thenReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(get("/orders").with(withoutAuthentication()))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.instance").value("/orders"));
  }

  @Test
  @DisplayName("GET /orders/{id} - Given order exists, when find by id, then returns order")
  void givenOrderExists_whenFindOrderById_thenReturnsOrder() throws Exception {
    String expectedJson = JsonUtils.readJson("orders/find-by-id.json");

    String responseJson =
        mockMvc
            .perform(get("/orders/{orderId}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /orders/{id} - Given order does not exist, when find by id, then returns not found")
  void givenOrderDoesNotExist_whenFindOrderById_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(get("/orders/{orderId}", 9999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Order not found"))
        .andExpect(jsonPath("$.instance").value("/orders/9999"));
  }

  @Test
  @DisplayName(
      "GET /orders/track/{orderNumber} - Given order exists, when find order by order number, then returns order tracking")
  void givenOrderExists_whenFindOrderByOrderNumber_thenReturnsOrderTracking() throws Exception {
    String expectedJson = JsonUtils.readJson("orders/find-tracking-by-order-number.json");

    String responseJson =
        mockMvc
            .perform(get("/orders/track/{orderNumber}", ORDER_NUMBER))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /orders/track/{orderNumber} - Given order does not exist, when find order by order number, then returns not found")
  void givenOrderDoesNotExist_whenFindOrderByOrderNumber_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(get("/orders/track/{orderNumber}", "00000000-0000-0000-0000-000000000000"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Order not found"))
        .andExpect(
            jsonPath("$.instance").value("/orders/track/00000000-0000-0000-0000-000000000000"));
  }

  @ParameterizedTest(name = "order {0}")
  @ValueSource(longs = {4L, 1L, 3L})
  @DisplayName(
      "PATCH /orders/{id}/cancel - Given cancellable order, when cancel, then returns no content and order is cancelled")
  void givenCancellableOrder_whenCancelOrder_thenReturnsNoContent(long orderId) throws Exception {
    mockMvc.perform(patch("/orders/{orderId}/cancel", orderId)).andExpect(status().isNoContent());

    mockMvc
        .perform(get("/orders/{orderId}", orderId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CANCELLED"));
  }

  @Test
  @DisplayName(
      "PATCH /orders/{id}/cancel - Given pending order, when cancel and flush, then cancellation is persisted with history")
  void givenPendingOrder_whenCancelOrderAndFlush_thenPersistsCancellationWithHistory()
      throws Exception {
    mockMvc.perform(patch("/orders/{orderId}/cancel", 4L)).andExpect(status().isNoContent());
    entityManager.flush();
    entityManager.clear();

    mockMvc
        .perform(get("/orders/{orderId}", 4L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CANCELLED"))
        .andExpect(jsonPath("$.status_history[*].notes", hasItem("Order cancelled")));
  }

  @ParameterizedTest(name = "order {0}")
  @ValueSource(longs = {2L, 5L})
  @DisplayName(
      "PATCH /orders/{id}/cancel - Given non cancellable order, when cancel, then returns unprocessable entity")
  void givenNonCancellableOrder_whenCancelOrder_thenReturnsUnprocessableEntity(long orderId)
      throws Exception {
    mockMvc
        .perform(patch("/orders/{orderId}/cancel", orderId))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.detail").value("Order cannot be canceled"))
        .andExpect(jsonPath("$.instance").value("/orders/" + orderId + "/cancel"));
  }

  @Test
  @DisplayName(
      "PATCH /orders/{id}/cancel - Given order does not exist, when cancel, then returns not found")
  void givenOrderDoesNotExist_whenCancelOrder_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(patch("/orders/{orderId}/cancel", 9999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Order not found"))
        .andExpect(jsonPath("$.instance").value("/orders/9999/cancel"));
  }
}
