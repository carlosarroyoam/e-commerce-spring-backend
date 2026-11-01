package com.carlosarroyoam.ecommerce.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.ecommerce.auth.principal.PrincipalType;
import com.carlosarroyoam.ecommerce.core.constant.MessageKeys;
import com.carlosarroyoam.ecommerce.core.exception.BusinessException;
import com.carlosarroyoam.ecommerce.core.exception.ExceptionLogger;
import com.carlosarroyoam.ecommerce.core.exception.GlobalExceptionHandler;
import com.carlosarroyoam.ecommerce.core.exception.ProblemDetailFactory;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.core.pagination.PaginationResponse;
import com.carlosarroyoam.ecommerce.support.security.FixedAuthPrincipalArgumentResolver;
import com.carlosarroyoam.ecommerce.support.testutils.TestMessageSources;
import com.carlosarroyoam.ecommerce.support.testutils.TestObjectMappers;
import com.carlosarroyoam.ecommerce.user.dto.RoleResponse;
import com.carlosarroyoam.ecommerce.user.dto.UserResponse;
import com.carlosarroyoam.ecommerce.user.dto.UserSpecs;
import com.carlosarroyoam.ecommerce.user.entity.UserStatus;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
  private static final long CURRENT_USER_ID = 1L;

  @Mock private UserService userService;
  @InjectMocks private UserController userController;

  private MockMvc mockMvc;
  private UserResponse user;

  @BeforeEach
  void setup() {
    user =
        UserResponse.builder()
            .id(2L)
            .firstName("Cathy")
            .lastName("Guido")
            .email("cathy.guido@e-commerce.com")
            .status(UserStatus.ACTIVE)
            .roles(
                List.of(
                    RoleResponse.builder()
                        .id((byte) 1)
                        .name("ADMIN")
                        .description("Admin user role")
                        .build()))
            .build();

    mockMvc =
        MockMvcBuilders.standaloneSetup(userController)
            .setControllerAdvice(
                new GlobalExceptionHandler(
                    new ProblemDetailFactory(),
                    new ExceptionLogger(),
                    TestMessageSources.messages()))
            .setMessageConverters(
                new MappingJackson2HttpMessageConverter(TestObjectMappers.snakeCase()))
            .setCustomArgumentResolvers(
                new PageableHandlerMethodArgumentResolver(),
                new FixedAuthPrincipalArgumentResolver(
                    CURRENT_USER_ID, PrincipalType.STAFF, "ADMIN"))
            .build();
  }

  @Test
  @DisplayName("GET /users - Given users exist, when find all, then returns paged users")
  void givenUsersExist_whenFindAllUsers_thenReturnsPagedUsers() throws Exception {
    PagedResponse<UserResponse> page =
        PagedResponse.<UserResponse>builder()
            .items(List.of(user))
            .pagination(
                PaginationResponse.builder().page(0).size(10).totalItems(1).totalPages(1).build())
            .build();

    when(userService.findAll(any(UserSpecs.class), any())).thenReturn(page);

    mockMvc
        .perform(
            get("/users")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].first_name").value("Cathy"))
        .andExpect(jsonPath("$.pagination.page").value(0))
        .andExpect(jsonPath("$.pagination.size").value(10))
        .andExpect(jsonPath("$.pagination.total_items").value(1))
        .andExpect(jsonPath("$.pagination.total_pages").value(1));
  }

  @Test
  @DisplayName("GET /users - Given first name filter, when find all, then passes filter to service")
  void givenFirstNameFilter_whenFindAllUsers_thenPassesFilterToService() throws Exception {
    PagedResponse<UserResponse> page =
        PagedResponse.<UserResponse>builder()
            .items(List.of())
            .pagination(
                PaginationResponse.builder().page(0).size(10).totalItems(0).totalPages(0).build())
            .build();

    when(userService.findAll(any(UserSpecs.class), any())).thenReturn(page);

    mockMvc.perform(get("/users").queryParam("firstName", "Cathy")).andExpect(status().isOk());

    verify(userService)
        .findAll(argThat((UserSpecs specs) -> "Cathy".equals(specs.getFirstName())), any());
  }

  @Test
  @DisplayName("GET /users/{id} - Given user exists, when find by id, then returns user")
  void givenUserExists_whenFindUserById_thenReturnsUser() throws Exception {
    when(userService.findById(anyLong())).thenReturn(user);

    mockMvc
        .perform(get("/users/{userId}", 2L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(2))
        .andExpect(jsonPath("$.email").value("cathy.guido@e-commerce.com"))
        .andExpect(jsonPath("$.roles[0].name").value("ADMIN"));
  }

  @Test
  @DisplayName(
      "GET /users/{id} - Given user does not exist, when find by id, then returns not found")
  void givenUserDoesNotExist_whenFindUserById_thenReturnsNotFound() throws Exception {
    when(userService.findById(anyLong()))
        .thenThrow(new ResourceNotFoundException(MessageKeys.USER_NOT_FOUND_EXCEPTION));

    mockMvc
        .perform(get("/users/{userId}", 999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("User not found"))
        .andExpect(jsonPath("$.instance").value("/users/999"));
  }

  @Test
  @DisplayName("DELETE /users/{id} - Given user exists, when delete, then returns no content")
  void givenUserExists_whenDeleteUser_thenReturnsNoContent() throws Exception {
    mockMvc.perform(delete("/users/{userId}", 2L)).andExpect(status().isNoContent());

    verify(userService).deleteById(2L, CURRENT_USER_ID);
  }

  @Test
  @DisplayName(
      "DELETE /users/{id} - Given user does not exist, when delete, then returns not found")
  void givenUserDoesNotExist_whenDeleteUser_thenReturnsNotFound() throws Exception {
    doThrow(new ResourceNotFoundException(MessageKeys.USER_NOT_FOUND_EXCEPTION))
        .when(userService)
        .deleteById(999L, CURRENT_USER_ID);

    mockMvc
        .perform(delete("/users/{userId}", 999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("User not found"))
        .andExpect(jsonPath("$.instance").value("/users/999"));
  }

  @Test
  @DisplayName(
      "DELETE /users/{id} - Given service rejects deletion, when delete, then returns unprocessable entity")
  void givenServiceRejectsDeletion_whenDeleteUser_thenReturnsUnprocessableEntity()
      throws Exception {
    doThrow(new BusinessException(MessageKeys.USER_CANNOT_DELETE_ITSELF_EXCEPTION))
        .when(userService)
        .deleteById(CURRENT_USER_ID, CURRENT_USER_ID);

    mockMvc
        .perform(delete("/users/{userId}", CURRENT_USER_ID))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.detail").value("User cannot delete itself"))
        .andExpect(jsonPath("$.instance").value("/users/1"));
  }
}
