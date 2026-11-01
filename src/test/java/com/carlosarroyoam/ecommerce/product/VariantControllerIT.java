package com.carlosarroyoam.ecommerce.product;

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
class VariantControllerIT extends AbstractIntegrationTest {
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
      "GET /products/{productId}/variants - Given product with variants, when find all, then returns variants")
  void givenProductWithVariants_whenFindAllVariants_thenReturnsVariants() throws Exception {
    String expectedJson = JsonUtils.readJson("variants/find-all-by-product-id.json");

    String responseJson =
        mockMvc
            .perform(get("/products/{productId}/variants", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /products/{productId}/variants - Given no authentication, when find all, then returns unauthorized")
  void givenNoAuthentication_whenFindAllVariants_thenReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(get("/products/{productId}/variants", 1L).with(withoutAuthentication()))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.instance").value("/products/1/variants"));
  }

  @Test
  @DisplayName(
      "GET /products/{productId}/variants - Given product does not exist, when find all, then returns not found")
  void givenProductDoesNotExist_whenFindAllVariants_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(get("/products/{productId}/variants", 9999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Product not found"))
        .andExpect(jsonPath("$.instance").value("/products/9999/variants"));
  }

  @Test
  @DisplayName(
      "GET /products/{productId}/variants/{variantId} - Given variant belongs to product, when find by id, then returns variant")
  void givenVariantBelongsToProduct_whenFindVariantById_thenReturnsVariant() throws Exception {
    String expectedJson = JsonUtils.readJson("variants/find-by-id.json");

    String responseJson =
        mockMvc
            .perform(get("/products/{productId}/variants/{variantId}", 1L, 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /products/{productId}/variants/{variantId} - Given product does not exist, when find by id, then returns product not found")
  void givenProductDoesNotExist_whenFindVariantById_thenReturnsProductNotFound() throws Exception {
    mockMvc
        .perform(get("/products/{productId}/variants/{variantId}", 9999L, 1L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Product not found"))
        .andExpect(jsonPath("$.instance").value("/products/9999/variants/1"));
  }

  @Test
  @DisplayName(
      "GET /products/{productId}/variants/{variantId} - Given variant does not exist, when find by id, then returns variant not found")
  void givenVariantDoesNotExist_whenFindVariantById_thenReturnsVariantNotFound() throws Exception {
    mockMvc
        .perform(get("/products/{productId}/variants/{variantId}", 1L, 9999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Product variant not found"))
        .andExpect(jsonPath("$.instance").value("/products/1/variants/9999"));
  }

  @Test
  @DisplayName(
      "GET /products/{productId}/variants/{variantId} - Given variant belongs to another product, when find by id, then returns variant not found")
  void givenVariantBelongsToAnotherProduct_whenFindVariantById_thenReturnsVariantNotFound()
      throws Exception {
    mockMvc
        .perform(get("/products/{productId}/variants/{variantId}", 1L, 3L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Product variant not found"))
        .andExpect(jsonPath("$.instance").value("/products/1/variants/3"));
  }
}
