package com.carlosarroyoam.ecommerce.refund;

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
class RefundControllerIT extends AbstractIntegrationTest {
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
  @DisplayName(
      "GET /refunds - Given no refunds in seed data, when find all, then returns empty page")
  void givenNoRefunds_whenFindAllRefunds_thenReturnsEmptyPage() throws Exception {
    String expectedJson = JsonUtils.readJson("refunds/find-all.json");

    String responseJson =
        mockMvc
            .perform(get("/refunds"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.items.length()").value(0))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("GET /refunds - Given order id filter, when find all, then returns empty page")
  void givenOrderIdFilter_whenFindAllRefunds_thenReturnsEmptyPage() throws Exception {
    mockMvc
        .perform(get("/refunds").queryParam("orderId", "1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.items.length()").value(0))
        .andExpect(jsonPath("$.pagination.total_items").value(0));
  }

  @Test
  @DisplayName("GET /refunds - Given no authentication, when find all, then returns unauthorized")
  void givenNoAuthentication_whenFindAllRefunds_thenReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(get("/refunds").with(withoutAuthentication()))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.instance").value("/refunds"));
  }

  @Test
  @DisplayName(
      "GET /refunds/{id} - Given refund does not exist, when find by id, then returns not found")
  void givenRefundDoesNotExist_whenFindRefundById_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(get("/refunds/{refundId}", 9999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Refund not found"))
        .andExpect(jsonPath("$.instance").value("/refunds/9999"));
  }

  @Test
  @DisplayName(
      "GET /refunds/order/{orderId} - Given order without refund, when find refunds by order id, then returns empty list")
  void givenOrderWithoutRefund_whenFindRefundsByOrderId_thenReturnsEmptyList() throws Exception {
    mockMvc
        .perform(get("/refunds/order/{orderId}", 1L))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.length()").value(0));
  }
}
