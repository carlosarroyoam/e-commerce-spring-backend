package com.carlosarroyoam.ecommerce.category;

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
class CategoryControllerIT extends AbstractIntegrationTest {
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
      "GET /categories - Given categories exist, when find all, then returns paged categories")
  void givenCategoriesExist_whenFindAllCategories_thenReturnsPagedCategories() throws Exception {
    String expectedJson = JsonUtils.readJson("categories/find-all.json");

    String responseJson =
        mockMvc
            .perform(get("/categories"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("GET /categories - Given page size 3, when find all, then returns second page")
  void givenPageSizeThree_whenFindAllCategories_thenReturnsSecondPage() throws Exception {
    String expectedJson = JsonUtils.readJson("categories/find-all-page-2.json");

    String responseJson =
        mockMvc
            .perform(get("/categories").queryParam("page", "1").queryParam("size", "3"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /categories - Given no authentication, when find all, then returns unauthorized")
  void givenNoAuthentication_whenFindAllCategories_thenReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(get("/categories").with(withoutAuthentication()))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.instance").value("/categories"));
  }

  @Test
  @DisplayName(
      "GET /categories/{id} - Given category exists, when find by id, then returns category")
  void givenCategoryExists_whenFindCategoryById_thenReturnsCategory() throws Exception {
    String expectedJson = JsonUtils.readJson("categories/find-by-id.json");

    String responseJson =
        mockMvc
            .perform(get("/categories/{categoryId}", 1))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /categories/{id} - Given category does not exist, when find by id, then returns not found")
  void givenCategoryDoesNotExist_whenFindCategoryById_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(get("/categories/{categoryId}", 99))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Category not found"))
        .andExpect(jsonPath("$.instance").value("/categories/99"));
  }

  @Test
  @DisplayName(
      "GET /categories/{id} - Given id out of byte range, when find by id, then returns bad request")
  void givenIdOutOfByteRange_whenFindCategoryById_thenReturnsBadRequest() throws Exception {
    mockMvc
        .perform(get("/categories/{categoryId}", 300))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(jsonPath("$.instance").value("/categories/300"));
  }
}
