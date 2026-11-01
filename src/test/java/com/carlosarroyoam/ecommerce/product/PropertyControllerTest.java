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

import com.carlosarroyoam.ecommerce.core.constant.MessageKeys;
import com.carlosarroyoam.ecommerce.core.exception.ExceptionLogger;
import com.carlosarroyoam.ecommerce.core.exception.GlobalExceptionHandler;
import com.carlosarroyoam.ecommerce.core.exception.ProblemDetailFactory;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.core.pagination.PaginationResponse;
import com.carlosarroyoam.ecommerce.product.dto.PropertyResponse;
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
class PropertyControllerTest {
  @Mock private PropertyService propertyService;
  @InjectMocks private PropertyController propertyController;

  private MockMvc mockMvc;
  private PropertyResponse property;

  @BeforeEach
  void setup() {
    property = PropertyResponse.builder().id(1L).title("Brand").build();

    mockMvc =
        MockMvcBuilders.standaloneSetup(propertyController)
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
      "GET /properties - Given properties exist, when find all, then returns paged properties")
  void givenPropertiesExist_whenFindAllProperties_thenReturnsPagedProperties() throws Exception {
    PagedResponse<PropertyResponse> page =
        PagedResponse.<PropertyResponse>builder()
            .items(List.of(property))
            .pagination(
                PaginationResponse.builder().page(0).size(10).totalItems(1).totalPages(1).build())
            .build();

    when(propertyService.findAll(any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(
            get("/properties")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].id").value(1))
        .andExpect(jsonPath("$.items[0].title").value("Brand"))
        .andExpect(jsonPath("$.items[0].deleted_at").doesNotExist())
        .andExpect(jsonPath("$.pagination.page").value(0))
        .andExpect(jsonPath("$.pagination.size").value(10))
        .andExpect(jsonPath("$.pagination.total_items").value(1))
        .andExpect(jsonPath("$.pagination.total_pages").value(1));
  }

  @Test
  @DisplayName(
      "GET /properties - Given pagination params, when find all, then passes pageable to service")
  void givenPaginationParams_whenFindAllProperties_thenPassesPageableToService() throws Exception {
    PagedResponse<PropertyResponse> page =
        PagedResponse.<PropertyResponse>builder()
            .items(List.of())
            .pagination(
                PaginationResponse.builder().page(2).size(3).totalItems(0).totalPages(0).build())
            .build();

    when(propertyService.findAll(any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(get("/properties").queryParam("page", "2").queryParam("size", "3"))
        .andExpect(status().isOk());

    verify(propertyService)
        .findAll(
            argThat((Pageable pageable) ->
                pageable.getPageNumber() == 2 && pageable.getPageSize() == 3));
  }

  @Test
  @DisplayName(
      "GET /properties/{id} - Given property exists, when find by id, then returns property")
  void givenPropertyExists_whenFindPropertyById_thenReturnsProperty() throws Exception {
    when(propertyService.findById(anyLong())).thenReturn(property);

    mockMvc
        .perform(get("/properties/{propertyId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Brand"));
  }

  @Test
  @DisplayName(
      "GET /properties/{id} - Given property does not exist, when find by id, then returns not found")
  void givenPropertyDoesNotExist_whenFindPropertyById_thenReturnsNotFound() throws Exception {
    when(propertyService.findById(anyLong()))
        .thenThrow(new ResourceNotFoundException(MessageKeys.PROPERTY_NOT_FOUND_EXCEPTION));

    mockMvc
        .perform(get("/properties/{propertyId}", 999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Property not found"))
        .andExpect(jsonPath("$.instance").value("/properties/999"));
  }
}
