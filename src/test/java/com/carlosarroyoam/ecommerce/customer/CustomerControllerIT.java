package com.carlosarroyoam.ecommerce.customer;

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
class CustomerControllerIT extends AbstractIntegrationTest {
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
      "GET /customers - Given customers exist, when find all, then returns paged customers")
  void givenCustomersExist_whenFindAllCustomers_thenReturnsPagedCustomers() throws Exception {
    mockMvc
        .perform(get("/customers").queryParam("size", "2"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.items.length()").value(2))
        .andExpect(jsonPath("$.items[0].id").value(1))
        .andExpect(jsonPath("$.items[1].id").value(2))
        .andExpect(jsonPath("$.pagination.page").value(0))
        .andExpect(jsonPath("$.pagination.size").value(2))
        .andExpect(jsonPath("$.pagination.total_items").value(100))
        .andExpect(jsonPath("$.pagination.total_pages").value(50));
  }

  @Test
  @DisplayName(
      "GET /customers - Given suspended status filter, when find all, then returns suspended customers")
  void givenSuspendedStatusFilter_whenFindAllCustomers_thenReturnsSuspendedCustomers()
      throws Exception {
    String expectedJson = JsonUtils.readJson("customers/find-all-suspended.json");

    String responseJson =
        mockMvc
            .perform(get("/customers").queryParam("status", "SUSPENDED").queryParam("size", "2"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /customers - Given pending status filter, when find all, then returns pending customers")
  void givenPendingStatusFilter_whenFindAllCustomers_thenReturnsPendingCustomers()
      throws Exception {
    mockMvc
        .perform(get("/customers").queryParam("status", "PENDING"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].id").value(2))
        .andExpect(jsonPath("$.items[0].email").value("maria.lopez@example.com"))
        .andExpect(jsonPath("$.items[0].status").value("PENDING"))
        .andExpect(jsonPath("$.pagination.total_items").value(1));
  }

  @Test
  @DisplayName(
      "GET /customers - Given name and email filters, when find all, then returns matching customers")
  void givenNameAndEmailFilters_whenFindAllCustomers_thenReturnsMatchingCustomers()
      throws Exception {
    mockMvc
        .perform(
            get("/customers")
                .queryParam("firstName", "Laura")
                .queryParam("lastName", "Ramírez")
                .queryParam("email", "laura.ramirez@example.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].id").value(6))
        .andExpect(jsonPath("$.items[0].first_name").value("Laura Isabel"))
        .andExpect(jsonPath("$.pagination.total_items").value(1));
  }

  @Test
  @DisplayName(
      "GET /customers - Given date range outside seed data, when find all, then returns empty page")
  void givenDateRangeOutsideSeedData_whenFindAllCustomers_thenReturnsEmptyPage() throws Exception {
    mockMvc
        .perform(
            get("/customers")
                .queryParam("startDate", "2000-01-01")
                .queryParam("endDate", "2000-12-31"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(0))
        .andExpect(jsonPath("$.pagination.total_items").value(0))
        .andExpect(jsonPath("$.pagination.total_pages").value(0));
  }

  @Test
  @DisplayName(
      "GET /customers - Given invalid email filter, when find all, then returns unprocessable entity")
  void givenInvalidEmailFilter_whenFindAllCustomers_thenReturnsUnprocessableEntity()
      throws Exception {
    mockMvc
        .perform(get("/customers").queryParam("email", "no-es-un-email"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.instance").value("/customers"))
        .andExpect(jsonPath("$.errors.email").value("Email should be an valid email address"));
  }

  @Test
  @DisplayName("GET /customers - Given no authentication, when find all, then returns unauthorized")
  void givenNoAuthentication_whenFindAllCustomers_thenReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(get("/customers").with(withoutAuthentication()))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.instance").value("/customers"));
  }

  @Test
  @DisplayName(
      "GET /customers/{id} - Given customer exists, when find by id, then returns customer")
  void givenCustomerExists_whenFindCustomerById_thenReturnsCustomer() throws Exception {
    String expectedJson = JsonUtils.readJson("customers/find-by-id.json");

    String responseJson =
        mockMvc
            .perform(get("/customers/{customerId}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /customers/{id} - Given customer does not exist, when find by id, then returns not found")
  void givenCustomerDoesNotExist_whenFindCustomerById_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(get("/customers/{customerId}", 9999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Customer not found"))
        .andExpect(jsonPath("$.instance").value("/customers/9999"));
  }
}
