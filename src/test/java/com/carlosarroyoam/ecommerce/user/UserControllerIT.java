package com.carlosarroyoam.ecommerce.user;

import static com.carlosarroyoam.ecommerce.support.security.AuthRequestPostProcessors.bearer;
import static com.carlosarroyoam.ecommerce.support.security.AuthRequestPostProcessors.withoutAuthentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserControllerIT extends AbstractIntegrationTest {
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
  @DisplayName("GET /users - Given users exist, when find all, then returns paged users")
  void givenUsersExist_whenFindAllUsers_thenReturnsPagedUsers() throws Exception {
    String expectedJson = JsonUtils.readJson("users/find-all.json");

    String responseJson =
        mockMvc
            .perform(get("/users"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /users - Given deleted status filter, when find all, then returns deleted users")
  void givenDeletedStatusFilter_whenFindAllUsers_thenReturnsDeletedUsers() throws Exception {
    String expectedJson = JsonUtils.readJson("users/find-all-deleted.json");

    String responseJson =
        mockMvc
            .perform(get("/users").queryParam("status", "DELETED"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("GET /users - Given no authentication, when find all, then returns unauthorized")
  void givenNoAuthentication_whenFindAllUsers_thenReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(get("/users").with(withoutAuthentication()))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.instance").value("/users"));
  }

  @Test
  @DisplayName("GET /users - Given customer role, when find all, then returns forbidden")
  void givenCustomerRole_whenFindAllUsers_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(get("/users").with(customerToken()))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.instance").value("/users"));
  }

  @Test
  @DisplayName("GET /users/{id} - Given user exists, when find by id, then returns user")
  void givenUserExists_whenFindUserById_thenReturnsUser() throws Exception {
    String expectedJson = JsonUtils.readJson("users/find-by-id.json");

    String responseJson =
        mockMvc
            .perform(get("/users/{userId}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /users/{id} - Given user does not exist, when find by id, then returns not found")
  void givenUserDoesNotExist_whenFindUserById_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(get("/users/{userId}", 9999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("User not found"))
        .andExpect(jsonPath("$.instance").value("/users/9999"));
  }

  @Test
  @DisplayName("GET /users/{id} - Given customer role, when find by id, then returns forbidden")
  void givenCustomerRole_whenFindUserById_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(get("/users/{userId}", 1L).with(customerToken()))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.instance").value("/users/1"));
  }

  @Test
  @DisplayName("DELETE /users/{id} - Given inactive user, when delete, then returns no content")
  void givenInactiveUser_whenDeleteUser_thenReturnsNoContent() throws Exception {
    mockMvc.perform(delete("/users/{userId}", 4L)).andExpect(status().isNoContent());

    mockMvc
        .perform(get("/users/{userId}", 4L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DELETED"));
  }

  @Test
  @DisplayName(
      "DELETE /users/{id} - Given user does not exist, when delete, then returns not found")
  void givenUserDoesNotExist_whenDeleteUser_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(delete("/users/{userId}", 9999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("User not found"))
        .andExpect(jsonPath("$.instance").value("/users/9999"));
  }

  @Test
  @DisplayName(
      "DELETE /users/{id} - Given admin deletes itself, when delete, then returns unprocessable entity")
  void givenAdminDeletesItself_whenDeleteUser_thenReturnsUnprocessableEntity() throws Exception {
    mockMvc
        .perform(delete("/users/{userId}", ADMIN_ID))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.detail").value("User cannot delete itself"))
        .andExpect(jsonPath("$.instance").value("/users/1"));
  }

  @Test
  @DisplayName(
      "DELETE /users/{id} - Given user already deleted, when delete, then returns unprocessable entity")
  void givenUserAlreadyDeleted_whenDeleteUser_thenReturnsUnprocessableEntity() throws Exception {
    mockMvc
        .perform(delete("/users/{userId}", 6L))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.detail").value("User cannot be deleted"))
        .andExpect(jsonPath("$.instance").value("/users/6"));
  }

  @Test
  @DisplayName("DELETE /users/{id} - Given customer role, when delete, then returns forbidden")
  void givenCustomerRole_whenDeleteUser_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(delete("/users/{userId}", 4L).with(customerToken()))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.instance").value("/users/4"));
  }

  private RequestPostProcessor customerToken() {
    return bearer(
        jwtTestTokenFactory.customerToken(1L, "jose.garcia@example.com", List.of("CUSTOMER")));
  }
}
