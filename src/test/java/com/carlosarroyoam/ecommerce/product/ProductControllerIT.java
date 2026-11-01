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
class ProductControllerIT extends AbstractIntegrationTest {
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
  @DisplayName("GET /products - Given products exist, when find all, then returns paged products")
  void givenProductsExist_whenFindAllProducts_thenReturnsPagedProducts() throws Exception {
    String expectedJson = JsonUtils.readJson("products/find-all.json");

    String responseJson =
        mockMvc
            .perform(get("/products").queryParam("size", "2"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /products - Given featured and category filters, when find all, then returns matching products")
  void givenFeaturedAndCategoryFilters_whenFindAllProducts_thenReturnsMatchingProducts()
      throws Exception {
    String expectedJson = JsonUtils.readJson("products/find-all-featured-category.json");

    String responseJson =
        mockMvc
            .perform(
                get("/products")
                    .queryParam("isFeatured", "true")
                    .queryParam("categoryId", "1")
                    .queryParam("size", "2"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /products - Given case-insensitive title filter, when find all, then returns matching products")
  void givenTitleFilter_whenFindAllProducts_thenReturnsMatchingProducts() throws Exception {
    String expectedJson = JsonUtils.readJson("products/find-all-by-title.json");

    String responseJson =
        mockMvc
            .perform(get("/products").queryParam("title", "GALAXY").queryParam("size", "2"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /products - Given slug filter, when find all, then returns the matching product")
  void givenSlugFilter_whenFindAllProducts_thenReturnsMatchingProduct() throws Exception {
    mockMvc
        .perform(get("/products").queryParam("slug", "playstation-5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].id").value(16))
        .andExpect(jsonPath("$.items[0].title").value("PlayStation 5"))
        .andExpect(jsonPath("$.pagination.total_items").value(1));
  }

  @Test
  @DisplayName("GET /products - Given inactive filter, when find all, then returns no products")
  void givenInactiveFilter_whenFindAllProducts_thenReturnsNoProducts() throws Exception {
    mockMvc
        .perform(get("/products").queryParam("isActive", "false"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(0))
        .andExpect(jsonPath("$.pagination.total_items").value(0))
        .andExpect(jsonPath("$.pagination.total_pages").value(0));
  }

  @Test
  @DisplayName(
      "GET /products - Given start date in the past, when find all, then returns all products")
  void givenPastStartDate_whenFindAllProducts_thenReturnsAllProducts() throws Exception {
    mockMvc
        .perform(get("/products").queryParam("startDate", "2000-01-01").queryParam("size", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.pagination.total_items").value(20));
  }

  @Test
  @DisplayName(
      "GET /products - Given end date in the past, when find all, then returns no products")
  void givenPastEndDate_whenFindAllProducts_thenReturnsNoProducts() throws Exception {
    mockMvc
        .perform(get("/products").queryParam("endDate", "2000-01-01"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(0))
        .andExpect(jsonPath("$.pagination.total_items").value(0));
  }

  @Test
  @DisplayName("GET /products - Given no authentication, when find all, then returns unauthorized")
  void givenNoAuthentication_whenFindAllProducts_thenReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(get("/products").with(withoutAuthentication()))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.instance").value("/products"));
  }

  @Test
  @DisplayName("GET /products/{id} - Given product exists, when find by id, then returns product")
  void givenProductExists_whenFindProductById_thenReturnsProduct() throws Exception {
    String expectedJson = JsonUtils.readJson("products/find-by-id.json");

    String responseJson =
        mockMvc
            .perform(get("/products/{productId}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /products/{id} - Given product does not exist, when find by id, then returns not found")
  void givenProductDoesNotExist_whenFindProductById_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(get("/products/{productId}", 9999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Product not found"))
        .andExpect(jsonPath("$.instance").value("/products/9999"));
  }

  @Test
  @DisplayName(
      "GET /products - Given title longer than 96 characters, when find all, then returns unprocessable entity")
  void givenTooLongTitleFilter_whenFindAllProducts_thenReturnsUnprocessableEntity()
      throws Exception {
    mockMvc
        .perform(get("/products").queryParam("title", "a".repeat(97)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.instance").value("/products"))
        .andExpect(jsonPath("$.errors.title").value("Title should be max 96"));
  }

  @Test
  @DisplayName(
      "GET /products - Given slug longer than 96 characters, when find all, then returns unprocessable entity")
  void givenTooLongSlugFilter_whenFindAllProducts_thenReturnsUnprocessableEntity()
      throws Exception {
    mockMvc
        .perform(get("/products").queryParam("slug", "a".repeat(97)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.errors.slug").value("Slug should be max 96"));
  }

  @Test
  @DisplayName(
      "GET /products - Given non-boolean featured filter, when find all, then returns unprocessable entity")
  void givenInvalidFeaturedFilter_whenFindAllProducts_thenReturnsUnprocessableEntity()
      throws Exception {
    mockMvc
        .perform(get("/products").queryParam("isFeatured", "abc"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.errors.isFeatured").exists());
  }

  @Test
  @DisplayName(
      "GET /products - Given out of range category id, when find all, then returns unprocessable entity")
  void givenOutOfRangeCategoryId_whenFindAllProducts_thenReturnsUnprocessableEntity()
      throws Exception {
    mockMvc
        .perform(get("/products").queryParam("categoryId", "999"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.errors.categoryId").exists());
  }

  @Test
  @DisplayName(
      "GET /products - Given malformed start date, when find all, then returns unprocessable entity")
  void givenMalformedStartDate_whenFindAllProducts_thenReturnsUnprocessableEntity()
      throws Exception {
    mockMvc
        .perform(get("/products").queryParam("startDate", "notadate"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.errors.startDate").exists());
  }

  @Test
  @DisplayName(
      "GET /products - Given unknown sort property, when find all, then returns bad request")
  void givenUnknownSortProperty_whenFindAllProducts_thenReturnsBadRequest() throws Exception {
    mockMvc
        .perform(get("/products").queryParam("sort", "nonexistent,asc"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(jsonPath("$.detail").value("Sort property is not valid"))
        .andExpect(jsonPath("$.instance").value("/products"));
  }

  @Test
  @DisplayName(
      "GET /products/{id} - Given non-numeric id, when find by id, then returns bad request")
  void givenNonNumericId_whenFindProductById_thenReturnsBadRequest() throws Exception {
    mockMvc
        .perform(get("/products/{productId}", "abc"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(jsonPath("$.instance").value("/products/abc"));
  }
}
