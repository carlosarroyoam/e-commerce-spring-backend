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
import com.carlosarroyoam.ecommerce.product.dto.AttributeResponse;
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
class AttributeControllerTest {
  @Mock private AttributeService attributeService;
  @InjectMocks private AttributeController attributeController;

  private MockMvc mockMvc;
  private AttributeResponse attribute;

  @BeforeEach
  void setup() {
    attribute = AttributeResponse.builder().id(1L).title("Color").build();

    mockMvc =
        MockMvcBuilders.standaloneSetup(attributeController)
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
      "GET /attributes - Given attributes exist, when find all, then returns paged attributes")
  void givenAttributesExist_whenFindAllAttributes_thenReturnsPagedAttributes() throws Exception {
    PagedResponse<AttributeResponse> page =
        PagedResponse.<AttributeResponse>builder()
            .items(List.of(attribute))
            .pagination(
                PaginationResponse.builder().page(0).size(10).totalItems(1).totalPages(1).build())
            .build();

    when(attributeService.findAll(any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(
            get("/attributes")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].id").value(1))
        .andExpect(jsonPath("$.items[0].title").value("Color"))
        .andExpect(jsonPath("$.items[0].deleted_at").doesNotExist())
        .andExpect(jsonPath("$.pagination.page").value(0))
        .andExpect(jsonPath("$.pagination.size").value(10))
        .andExpect(jsonPath("$.pagination.total_items").value(1))
        .andExpect(jsonPath("$.pagination.total_pages").value(1));
  }

  @Test
  @DisplayName(
      "GET /attributes - Given pagination params, when find all, then passes pageable to service")
  void givenPaginationParams_whenFindAllAttributes_thenPassesPageableToService() throws Exception {
    PagedResponse<AttributeResponse> page =
        PagedResponse.<AttributeResponse>builder()
            .items(List.of())
            .pagination(
                PaginationResponse.builder().page(2).size(3).totalItems(0).totalPages(0).build())
            .build();

    when(attributeService.findAll(any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(get("/attributes").queryParam("page", "2").queryParam("size", "3"))
        .andExpect(status().isOk());

    verify(attributeService)
        .findAll(
            argThat((Pageable pageable) ->
                pageable.getPageNumber() == 2 && pageable.getPageSize() == 3));
  }

  @Test
  @DisplayName(
      "GET /attributes/{id} - Given attribute exists, when find by id, then returns attribute")
  void givenAttributeExists_whenFindAttributeById_thenReturnsAttribute() throws Exception {
    when(attributeService.findById(anyLong())).thenReturn(attribute);

    mockMvc
        .perform(get("/attributes/{attributeId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Color"));
  }

  @Test
  @DisplayName(
      "GET /attributes/{id} - Given attribute does not exist, when find by id, then returns not found")
  void givenAttributeDoesNotExist_whenFindAttributeById_thenReturnsNotFound() throws Exception {
    when(attributeService.findById(anyLong()))
        .thenThrow(new ResourceNotFoundException(MessageKeys.ATTRIBUTE_NOT_FOUND_EXCEPTION));

    mockMvc
        .perform(get("/attributes/{attributeId}", 999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Attribute not found"))
        .andExpect(jsonPath("$.instance").value("/attributes/999"));
  }
}
