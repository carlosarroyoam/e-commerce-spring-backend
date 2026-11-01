package com.carlosarroyoam.ecommerce.category;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.ecommerce.category.dto.CategoryResponse;
import com.carlosarroyoam.ecommerce.core.constant.MessageKeys;
import com.carlosarroyoam.ecommerce.core.exception.ExceptionLogger;
import com.carlosarroyoam.ecommerce.core.exception.GlobalExceptionHandler;
import com.carlosarroyoam.ecommerce.core.exception.ProblemDetailFactory;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.core.pagination.PaginationResponse;
import com.carlosarroyoam.ecommerce.support.testutils.TestMessageSources;
import com.carlosarroyoam.ecommerce.support.testutils.TestObjectMappers;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {
  @Mock private CategoryService categoryService;
  @InjectMocks private CategoryController categoryController;

  private MockMvc mockMvc;
  private CategoryResponse category;

  @BeforeEach
  void setup() {
    category =
        CategoryResponse.builder().id((byte) 1).title("Smartphones").slug("smartphones").build();

    mockMvc =
        MockMvcBuilders.standaloneSetup(categoryController)
            .setControllerAdvice(
                new GlobalExceptionHandler(
                    new ProblemDetailFactory(),
                    new ExceptionLogger(),
                    TestMessageSources.messages()))
            .setMessageConverters(
                new MappingJackson2HttpMessageConverter(TestObjectMappers.snakeCase()))
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
  }

  @Test
  @DisplayName(
      "GET /categories - Given categories exist, when find all, then returns paged categories")
  void givenCategoriesExist_whenFindAllCategories_thenReturnsPagedCategories() throws Exception {
    PagedResponse<CategoryResponse> page =
        PagedResponse.<CategoryResponse>builder()
            .items(List.of(category))
            .pagination(
                PaginationResponse.builder().page(0).size(10).totalItems(1).totalPages(1).build())
            .build();

    when(categoryService.findAll(any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(
            get("/categories")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].id").value(1))
        .andExpect(jsonPath("$.items[0].title").value("Smartphones"))
        .andExpect(jsonPath("$.items[0].slug").value("smartphones"))
        .andExpect(jsonPath("$.items[0].deleted_at").doesNotExist())
        .andExpect(jsonPath("$.pagination.page").value(0))
        .andExpect(jsonPath("$.pagination.size").value(10))
        .andExpect(jsonPath("$.pagination.total_items").value(1))
        .andExpect(jsonPath("$.pagination.total_pages").value(1));
  }

  @Test
  @DisplayName(
      "GET /categories - Given pagination params, when find all, then passes pageable to service")
  void givenPaginationParams_whenFindAllCategories_thenPassesPageableToService() throws Exception {
    PagedResponse<CategoryResponse> page =
        PagedResponse.<CategoryResponse>builder()
            .items(List.of())
            .pagination(
                PaginationResponse.builder().page(2).size(3).totalItems(0).totalPages(0).build())
            .build();

    when(categoryService.findAll(any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(get("/categories").queryParam("page", "2").queryParam("size", "3"))
        .andExpect(status().isOk());

    verify(categoryService)
        .findAll(
            argThat((Pageable pageable) ->
                pageable.getPageNumber() == 2 && pageable.getPageSize() == 3));
  }

  @Test
  @DisplayName(
      "GET /categories/{id} - Given category exists, when find by id, then returns category")
  void givenCategoryExists_whenFindCategoryById_thenReturnsCategory() throws Exception {
    when(categoryService.findById(anyByte())).thenReturn(category);

    mockMvc
        .perform(get("/categories/{categoryId}", 1).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Smartphones"))
        .andExpect(jsonPath("$.slug").value("smartphones"));
  }

  @Test
  @DisplayName(
      "GET /categories/{id} - Given category does not exist, when find by id, then returns not found")
  void givenCategoryDoesNotExist_whenFindCategoryById_thenReturnsNotFound() throws Exception {
    when(categoryService.findById(anyByte()))
        .thenThrow(new ResourceNotFoundException(MessageKeys.CATEGORY_NOT_FOUND_EXCEPTION));

    mockMvc
        .perform(get("/categories/{categoryId}", 99))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Category not found"))
        .andExpect(jsonPath("$.instance").value("/categories/99"));
  }
}
