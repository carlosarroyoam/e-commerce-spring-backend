package com.carlosarroyoam.ecommerce.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.ecommerce.support.AbstractIntegrationTest;
import com.carlosarroyoam.ecommerce.support.security.JwtTestTokenFactory;
import com.carlosarroyoam.ecommerce.support.testutils.JsonUtils;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/** Integracion end-to-end de {@link UserController} contra MySQL real (Testcontainers). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class UserControllerIT extends AbstractIntegrationTest {
  private static final long ADMIN_STAFF_ID = 1L;
  private static final String ADMIN_STAFF_EMAIL = "carlos.arroyo@e-commerce.com";

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private JwtTestTokenFactory jwtTestTokenFactory;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    String token =
        jwtTestTokenFactory.staffToken(ADMIN_STAFF_ID, ADMIN_STAFF_EMAIL, List.of("ADMIN"));

    mockMvc =
        MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .defaultRequest(get("/").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .build();
  }

  @Test
  @DisplayName("GET /users - Given no filters, when find all, then returns the paged users")
  void givenNoFilters_whenFindAll_thenReturns200() throws Exception {
    String expected = JsonUtils.readJson("users/find-all.json");

    String response =
        mockMvc
            .perform(get("/users"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(StandardCharsets.UTF_8);

    JSONAssert.assertEquals(expected, response, false);
  }

  @Test
  @DisplayName(
      "GET /users - Given a status filter of DELETED, when find all, then returns only the deleted users")
  void givenDeletedStatusFilter_whenFindAll_thenReturnsOnlyDeletedUsers() throws Exception {
    String expected = JsonUtils.readJson("users/find-all-deleted.json");

    String response =
        mockMvc
            .perform(get("/users").param("status", "DELETED"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(StandardCharsets.UTF_8);

    JSONAssert.assertEquals(expected, response, false);
  }

  @Test
  @DisplayName(
      "GET /users/{userId} - Given existing user id, when find by id, then returns the user")
  void givenExistingUserId_whenFindById_thenReturnsTheUser() throws Exception {
    String expected = JsonUtils.readJson("users/find-by-id.json");

    String response =
        mockMvc
            .perform(get("/users/1"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(StandardCharsets.UTF_8);

    JSONAssert.assertEquals(expected, response, false);
  }

  @Test
  @DisplayName(
      "GET /users/{userId} - Given a non existing user id, when find by id, then returns 404")
  void givenNonExistingUserId_whenFindById_thenReturns404() throws Exception {
    mockMvc
        .perform(get("/users/999999"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.instance").value("/users/999999"))
        .andExpect(jsonPath("$.detail").value("User not found"));
  }

  @Test
  @DisplayName(
      "DELETE /users/{userId} - Given the authenticated admin tries to delete itself, when delete by id, then returns 422")
  void givenAdminTriesToDeleteItself_whenDeleteById_thenReturns422() throws Exception {
    mockMvc
        .perform(delete("/users/" + ADMIN_STAFF_ID))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.detail").value("User cannot delete itself"));
  }

  @Test
  @DisplayName(
      "DELETE /users/{userId} - Given an already deleted user, when delete by id, then returns 422")
  void givenAlreadyDeletedUser_whenDeleteById_thenReturns422() throws Exception {
    mockMvc
        .perform(delete("/users/6"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.detail").value("User cannot be deleted"));
  }

  @Test
  @DisplayName(
      "DELETE /users/{userId} - Given an inactive user, when delete by id, then returns 204")
  void givenInactiveUser_whenDeleteById_thenReturns204AndMarksItDeleted() throws Exception {
    mockMvc.perform(delete("/users/4")).andExpect(status().isNoContent());

    mockMvc.perform(get("/users/4")).andExpect(jsonPath("$.status").value("DELETED"));
  }
}
