package com.carlosarroyoam.ecommerce.payment;

import static com.carlosarroyoam.ecommerce.support.security.AuthRequestPostProcessors.withoutAuthentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.ecommerce.support.AbstractIntegrationTest;
import com.carlosarroyoam.ecommerce.support.security.JwtTestTokenFactory;
import com.carlosarroyoam.ecommerce.support.testutils.JsonUtils;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
class PaymentControllerIT extends AbstractIntegrationTest {
  private static final long ADMIN_ID = 1L;
  private static final String ADMIN_EMAIL = "carlos.arroyo@e-commerce.com";

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private JwtTestTokenFactory jwtTestTokenFactory;

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
  @DisplayName("GET /payments - Given payments exist, when find all, then returns paged payments")
  void givenPaymentsExist_whenFindAllPayments_thenReturnsPagedPayments() throws Exception {
    String expectedJson = JsonUtils.readJson("payments/find-all.json");

    String responseJson =
        mockMvc
            .perform(get("/payments"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /payments - Given reference filter, when find all, then returns payments with reference")
  void givenReferenceFilter_whenFindAllPayments_thenReturnsPaymentsWithReference()
      throws Exception {
    mockMvc
        .perform(get("/payments").queryParam("reference", "123456789012"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.items.length()").value(2))
        .andExpect(jsonPath("$.items[0].id").value(1))
        .andExpect(jsonPath("$.items[1].id").value(2))
        .andExpect(jsonPath("$.pagination.total_items").value(2));
  }

  @Test
  @DisplayName(
      "GET /payments - Given method filter, when find all, then returns payments with method")
  void givenMethodFilter_whenFindAllPayments_thenReturnsPaymentsWithMethod() throws Exception {
    mockMvc
        .perform(get("/payments").queryParam("method", "DEBIT_CARD"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.items.length()").value(2))
        .andExpect(jsonPath("$.items[0].id").value(1))
        .andExpect(jsonPath("$.items[1].id").value(5))
        .andExpect(jsonPath("$.pagination.total_items").value(2));
  }

  @Test
  @DisplayName(
      "GET /payments - Given status filter, when find all, then returns payments with status")
  void givenStatusFilter_whenFindAllPayments_thenReturnsPaymentsWithStatus() throws Exception {
    mockMvc
        .perform(get("/payments").queryParam("status", "COMPLETED"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.items.length()").value(4))
        .andExpect(jsonPath("$.pagination.total_items").value(4));

    mockMvc
        .perform(get("/payments").queryParam("status", "PENDING"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(0))
        .andExpect(jsonPath("$.pagination.total_items").value(0));
  }

  @Test
  @DisplayName(
      "GET /payments - Given order id filter, when find all, then returns payments of order")
  void givenOrderIdFilter_whenFindAllPayments_thenReturnsPaymentsOfOrder() throws Exception {
    mockMvc
        .perform(get("/payments").queryParam("orderId", "3"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].id").value(3))
        .andExpect(jsonPath("$.items[0].order_id").value(3))
        .andExpect(jsonPath("$.pagination.total_items").value(1));
  }

  @Test
  @DisplayName("GET /payments - Given no authentication, when find all, then returns unauthorized")
  void givenNoAuthentication_whenFindAllPayments_thenReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(get("/payments").with(withoutAuthentication()))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.instance").value("/payments"));
  }

  @Test
  @DisplayName("GET /payments/{id} - Given payment exists, when find by id, then returns payment")
  void givenPaymentExists_whenFindPaymentById_thenReturnsPayment() throws Exception {
    String expectedJson = JsonUtils.readJson("payments/find-by-id.json");

    String responseJson =
        mockMvc
            .perform(get("/payments/{paymentId}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /payments/{id} - Given payment does not exist, when find by id, then returns not found")
  void givenPaymentDoesNotExist_whenFindPaymentById_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(get("/payments/{paymentId}", 9999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Payment not found"))
        .andExpect(jsonPath("$.instance").value("/payments/9999"));
  }
}
