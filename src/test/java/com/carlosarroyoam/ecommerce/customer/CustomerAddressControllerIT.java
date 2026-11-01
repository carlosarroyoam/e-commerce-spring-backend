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
class CustomerAddressControllerIT extends AbstractIntegrationTest {
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
      "GET /customers/{id}/addresses - Given customer has addresses, when find all, then returns addresses")
  void givenCustomerHasAddresses_whenFindAllCustomerAddresses_thenReturnsAddresses()
      throws Exception {
    String expectedJson = JsonUtils.readJson("customer-addresses/find-all.json");

    String responseJson =
        mockMvc
            .perform(get("/customers/{customerId}/addresses", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.length()").value(1))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /customers/{id}/addresses - Given customer does not exist, when find all, then returns not found")
  void givenCustomerDoesNotExist_whenFindAllCustomerAddresses_thenReturnsNotFound()
      throws Exception {
    mockMvc
        .perform(get("/customers/{customerId}/addresses", 9999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Customer not found"))
        .andExpect(jsonPath("$.instance").value("/customers/9999/addresses"));
  }

  @Test
  @DisplayName(
      "GET /customers/{id}/addresses - Given no authentication, when find all, then returns unauthorized")
  void givenNoAuthentication_whenFindAllCustomerAddresses_thenReturnsUnauthorized()
      throws Exception {
    mockMvc
        .perform(get("/customers/{customerId}/addresses", 1L).with(withoutAuthentication()))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.instance").value("/customers/1/addresses"));
  }

  @Test
  @DisplayName(
      "GET /customers/{id}/addresses/{id} - Given address belongs to customer, when find by id, then returns address")
  void givenAddressBelongsToCustomer_whenFindCustomerAddressById_thenReturnsAddress()
      throws Exception {
    String expectedJson = JsonUtils.readJson("customer-addresses/find-by-id.json");

    String responseJson =
        mockMvc
            .perform(get("/customers/{customerId}/addresses/{addressId}", 2L, 2L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /customers/{id}/addresses/{id} - Given customer does not exist, when find by id, then returns customer not found")
  void givenCustomerDoesNotExist_whenFindCustomerAddressById_thenReturnsCustomerNotFound()
      throws Exception {
    mockMvc
        .perform(get("/customers/{customerId}/addresses/{addressId}", 9999L, 1L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Customer not found"))
        .andExpect(jsonPath("$.instance").value("/customers/9999/addresses/1"));
  }

  @Test
  @DisplayName(
      "GET /customers/{id}/addresses/{id} - Given address does not exist, when find by id, then returns address not found")
  void givenAddressDoesNotExist_whenFindCustomerAddressById_thenReturnsAddressNotFound()
      throws Exception {
    mockMvc
        .perform(get("/customers/{customerId}/addresses/{addressId}", 1L, 9999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Customer address not found"))
        .andExpect(jsonPath("$.instance").value("/customers/1/addresses/9999"));
  }

  @Test
  @DisplayName(
      "GET /customers/{id}/addresses/{id} - Given address belongs to another customer, when find by id, then returns address not found")
  void givenAddressBelongsToAnotherCustomer_whenFindCustomerAddressById_thenReturnsAddressNotFound()
      throws Exception {
    mockMvc
        .perform(get("/customers/{customerId}/addresses/{addressId}", 1L, 2L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Customer address not found"))
        .andExpect(jsonPath("$.instance").value("/customers/1/addresses/2"));
  }
}
