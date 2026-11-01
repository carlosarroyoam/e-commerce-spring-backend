package com.carlosarroyoam.ecommerce.product;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import com.carlosarroyoam.ecommerce.product.dto.ProductResponse;
import com.carlosarroyoam.ecommerce.product.dto.ProductSpecs;
import com.carlosarroyoam.ecommerce.support.testutils.TestMessageSources;
import com.carlosarroyoam.ecommerce.support.testutils.TestObjectMappers;
import java.time.LocalDate;
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
class ProductControllerTest {
  @Mock private ProductService productService;
  @InjectMocks private ProductController productController;

  private MockMvc mockMvc;
  private ProductResponse product;

  @BeforeEach
  void setup() {
    product =
        ProductResponse.builder()
            .id(1L)
            .title("Moto G100")
            .slug("moto-g100")
            .description("Moto G100")
            .isFeatured(false)
            .isActive(true)
            .category(
                CategoryResponse.builder()
                    .id((byte) 1)
                    .title("Smartphones")
                    .slug("smartphones")
                    .build())
            .properties(List.of())
            .variants(List.of())
            .build();

    mockMvc =
        MockMvcBuilders.standaloneSetup(productController)
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
  @DisplayName("GET /products - Given products exist, when find all, then returns paged products")
  void givenProductsExist_whenFindAllProducts_thenReturnsPagedProducts() throws Exception {
    PagedResponse<ProductResponse> page =
        PagedResponse.<ProductResponse>builder()
            .items(List.of(product))
            .pagination(
                PaginationResponse.builder().page(0).size(10).totalItems(1).totalPages(1).build())
            .build();

    when(productService.findAll(any(ProductSpecs.class), any())).thenReturn(page);

    mockMvc
        .perform(
            get("/products")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].title").value("Moto G100"))
        .andExpect(jsonPath("$.items[0].is_featured").value(false))
        .andExpect(jsonPath("$.items[0].is_active").value(true))
        .andExpect(jsonPath("$.items[0].category.slug").value("smartphones"))
        .andExpect(jsonPath("$.pagination.page").value(0))
        .andExpect(jsonPath("$.pagination.size").value(10))
        .andExpect(jsonPath("$.pagination.total_items").value(1))
        .andExpect(jsonPath("$.pagination.total_pages").value(1));
  }

  @Test
  @DisplayName("GET /products - Given title filter, when find all, then passes filter to service")
  void givenTitleFilter_whenFindAllProducts_thenPassesFilterToService() throws Exception {
    PagedResponse<ProductResponse> page =
        PagedResponse.<ProductResponse>builder()
            .items(List.of())
            .pagination(
                PaginationResponse.builder().page(0).size(10).totalItems(0).totalPages(0).build())
            .build();

    when(productService.findAll(any(ProductSpecs.class), any())).thenReturn(page);

    mockMvc.perform(get("/products").queryParam("title", "Moto")).andExpect(status().isOk());

    verify(productService)
        .findAll(argThat((ProductSpecs specs) -> "Moto".equals(specs.getTitle())), any());
  }

  @Test
  @DisplayName("GET /products - Given every filter, when find all, then binds all filters to specs")
  void givenEveryFilter_whenFindAllProducts_thenBindsAllFiltersToSpecs() throws Exception {
    PagedResponse<ProductResponse> page =
        PagedResponse.<ProductResponse>builder()
            .items(List.of())
            .pagination(
                PaginationResponse.builder().page(0).size(10).totalItems(0).totalPages(0).build())
            .build();

    when(productService.findAll(any(ProductSpecs.class), any())).thenReturn(page);

    mockMvc
        .perform(
            get("/products")
                .queryParam("title", "Moto")
                .queryParam("slug", "moto-g100")
                .queryParam("isFeatured", "true")
                .queryParam("isActive", "false")
                .queryParam("categoryId", "1")
                .queryParam("startDate", "2025-01-01")
                .queryParam("endDate", "2025-12-31"))
        .andExpect(status().isOk());

    verify(productService)
        .findAll(
            argThat((ProductSpecs specs) ->
                "Moto".equals(specs.getTitle())
                    && "moto-g100".equals(specs.getSlug())
                    && Boolean.TRUE.equals(specs.getIsFeatured())
                    && Boolean.FALSE.equals(specs.getIsActive())
                    && Byte.valueOf((byte) 1).equals(specs.getCategoryId())
                    && LocalDate.of(2025, 1, 1).equals(specs.getStartDate())
                    && LocalDate.of(2025, 12, 31).equals(specs.getEndDate())),
            any());
  }

  @Test
  @DisplayName("GET /products/{id} - Given product exists, when find by id, then returns product")
  void givenProductExists_whenFindProductById_thenReturnsProduct() throws Exception {
    when(productService.findById(anyLong())).thenReturn(product);

    mockMvc
        .perform(get("/products/{productId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Moto G100"))
        .andExpect(jsonPath("$.category.title").value("Smartphones"))
        .andExpect(jsonPath("$.variants.length()").value(0));
  }

  @Test
  @DisplayName(
      "GET /products/{id} - Given product does not exist, when find by id, then returns not found")
  void givenProductDoesNotExist_whenFindProductById_thenReturnsNotFound() throws Exception {
    when(productService.findById(anyLong()))
        .thenThrow(new ResourceNotFoundException(MessageKeys.PRODUCT_NOT_FOUND_EXCEPTION));

    mockMvc
        .perform(get("/products/{productId}", 999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Product not found"))
        .andExpect(jsonPath("$.instance").value("/products/999"));
  }
}
