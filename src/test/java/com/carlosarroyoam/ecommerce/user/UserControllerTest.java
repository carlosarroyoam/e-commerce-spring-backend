package com.carlosarroyoam.ecommerce.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.ecommerce.auth.principal.PrincipalType;
import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.exception.BusinessException;
import com.carlosarroyoam.ecommerce.core.exception.ExceptionLogger;
import com.carlosarroyoam.ecommerce.core.exception.GlobalExceptionHandler;
import com.carlosarroyoam.ecommerce.core.exception.ProblemDetailFactory;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.core.pagination.PaginationResponse;
import com.carlosarroyoam.ecommerce.support.security.FixedAuthPrincipalArgumentResolver;
import com.carlosarroyoam.ecommerce.support.testutils.TestObjectMappers;
import com.carlosarroyoam.ecommerce.user.dto.RoleResponse;
import com.carlosarroyoam.ecommerce.user.dto.UserResponse;
import com.carlosarroyoam.ecommerce.user.dto.UserSpecs;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

/** Slice unitario de {@link UserController} via {@code MockMvc.standaloneSetup}. */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {
  private static final long FIXED_STAFF_PRINCIPAL_ID = 1L;
  private MockMvc mockMvc;

  @Mock private UserService userService;

  private static UserResponse sampleUser(Long id) {
    return UserResponse.builder()
        .id(id)
        .firstName("Carlos Alberto")
        .lastName("Arroyo Martinez")
        .email("carlos.arroyo@e-commerce.com")
        .status(com.carlosarroyoam.ecommerce.user.entity.UserStatus.ACTIVE)
        .roles(
            List.of(
                RoleResponse.builder()
                    .id((byte) 1)
                    .name("ADMIN")
                    .description("Admin user role")
                    .build()))
        .build();
  }

  @BeforeEach
  void setUp() {
    UserController userController = new UserController(userService);

    HandlerMethodArgumentResolver authPrincipalResolver =
        new FixedAuthPrincipalArgumentResolver(
            FIXED_STAFF_PRINCIPAL_ID, PrincipalType.STAFF, "ADMIN");

    mockMvc =
        MockMvcBuilders.standaloneSetup(userController)
            .setControllerAdvice(
                new GlobalExceptionHandler(new ProblemDetailFactory(), new ExceptionLogger()))
            .setCustomArgumentResolvers(
                new PageableHandlerMethodArgumentResolver(), authPrincipalResolver)
            .setMessageConverters(
                new MappingJackson2HttpMessageConverter(TestObjectMappers.snakeCase()))
            .build();
  }

  @Test
  @DisplayName("Given no filters, when find all, then returns 200 with paged users")
  void givenNoFilters_whenFindAll_thenReturns200() throws Exception {
    PagedResponse<UserResponse> page =
        PagedResponse.<UserResponse>builder()
            .items(List.of(sampleUser(1L), sampleUser(2L)))
            .pagination(
                PaginationResponse.builder().page(0).size(10).totalItems(2).totalPages(1).build())
            .build();
    given(userService.findAll(any(), any())).willReturn(page);

    mockMvc
        .perform(get("/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(2))
        .andExpect(jsonPath("$.items[0].first_name").value("Carlos Alberto"))
        .andExpect(jsonPath("$.pagination.total_items").value(2));
  }

  @Test
  @DisplayName("Given a first name filter, when find all, then passes it to the service")
  void givenFirstNameFilter_whenFindAll_thenPassesFilterToService() throws Exception {
    given(userService.findAll(any(), any()))
        .willReturn(
            PagedResponse.<UserResponse>builder()
                .items(List.of())
                .pagination(
                    PaginationResponse.builder()
                        .page(0)
                        .size(10)
                        .totalItems(0)
                        .totalPages(0)
                        .build())
                .build());

    mockMvc.perform(get("/users").param("firstName", "Carlos")).andExpect(status().isOk());

    verify(userService)
        .findAll(argThat((UserSpecs specs) -> "Carlos".equals(specs.getFirstName())), any());
  }

  @Test
  @DisplayName("Given an existing user id, when find by id, then returns 200 with the user")
  void givenExistingUserId_whenFindById_thenReturns200WithUser() throws Exception {
    given(userService.findById(1L)).willReturn(sampleUser(1L));

    mockMvc
        .perform(get("/users/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("carlos.arroyo@e-commerce.com"))
        .andExpect(jsonPath("$.roles[0].name").value("ADMIN"));
  }

  @Test
  @DisplayName("Given a non existing user id, when find by id, then returns 404")
  void givenNonExistingUserId_whenFindById_thenReturns404() throws Exception {
    given(userService.findById(999L))
        .willThrow(new ResourceNotFoundException(AppMessages.USER_NOT_FOUND_EXCEPTION));

    mockMvc
        .perform(get("/users/999"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.instance").value("/users/999"))
        .andExpect(jsonPath("$.detail").value(AppMessages.USER_NOT_FOUND_EXCEPTION));
  }

  @Test
  @DisplayName("Given a user id, when delete by id, then returns 204")
  void givenUserId_whenDeleteById_thenReturns204AndDelegatesWithCurrentUserId() throws Exception {
    mockMvc.perform(delete("/users/5")).andExpect(status().isNoContent());

    verify(userService).deleteById(5L, FIXED_STAFF_PRINCIPAL_ID);
  }

  @Test
  @DisplayName("Given the service throws unprocessable entity, when delete by id, then returns 422")
  void
      givenServiceThrowsUnprocessableEntity_whenDeleteById_thenReturns422ViaGlobalExceptionHandler()
          throws Exception {
    doThrow(new BusinessException(AppMessages.USER_CANNOT_DELETE_ITSELF_EXCEPTION))
        .when(userService)
        .deleteById(1L, FIXED_STAFF_PRINCIPAL_ID);

    mockMvc
        .perform(delete("/users/1"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.detail").value(AppMessages.USER_CANNOT_DELETE_ITSELF_EXCEPTION));
  }
}
