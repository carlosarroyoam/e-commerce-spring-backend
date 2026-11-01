package com.carlosarroyoam.ecommerce.product;

import static org.mockito.ArgumentMatchers.anyLong;
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
import com.carlosarroyoam.ecommerce.product.dto.AttributeResponse;
import com.carlosarroyoam.ecommerce.product.dto.VariantAttributeValueResponse;
import com.carlosarroyoam.ecommerce.product.dto.VariantImageResponse;
import com.carlosarroyoam.ecommerce.product.dto.VariantResponse;
import com.carlosarroyoam.ecommerce.support.testutils.TestMessageSources;
import com.carlosarroyoam.ecommerce.support.testutils.TestObjectMappers;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class VariantControllerTest {
  @Mock private VariantService variantService;
  @InjectMocks private VariantController variantController;

  private MockMvc mockMvc;
  private VariantResponse variant;

  @BeforeEach
  void setup() {
    variant =
        VariantResponse.builder()
            .id(1L)
            .sku("motog100nimbusblue")
            .price(new BigDecimal("8999.00"))
            .comparedAtPrice(new BigDecimal("9999.00"))
            .attributes(
                List.of(
                    VariantAttributeValueResponse.builder()
                        .id(1L)
                        .value("Blue Nimbus")
                        .attribute(AttributeResponse.builder().id(1L).title("Color").build())
                        .build()))
            .images(
                List.of(
                    VariantImageResponse.builder().id(1L).url("motog100nimbusblue.jpg").build()))
            .build();

    mockMvc =
        MockMvcBuilders.standaloneSetup(variantController)
            .setControllerAdvice(
                new GlobalExceptionHandler(
                    new ProblemDetailFactory(),
                    new ExceptionLogger(),
                    TestMessageSources.messages()))
            .setMessageConverters(
                new MappingJackson2HttpMessageConverter(TestObjectMappers.snakeCase()))
            .build();
  }

  @Test
  @DisplayName(
      "GET /products/{productId}/variants - Given product with variants, when find all, then returns variants")
  void givenProductWithVariants_whenFindAllVariants_thenReturnsVariants() throws Exception {
    when(variantService.findAllByProductId(anyLong())).thenReturn(List.of(variant));

    mockMvc
        .perform(get("/products/{productId}/variants", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].sku").value("motog100nimbusblue"))
        .andExpect(jsonPath("$[0].price").value(8999.00))
        .andExpect(jsonPath("$[0].compared_at_price").value(9999.00))
        .andExpect(jsonPath("$[0].attributes[0].value").value("Blue Nimbus"))
        .andExpect(jsonPath("$[0].attributes[0].attribute.title").value("Color"))
        .andExpect(jsonPath("$[0].images[0].url").value("motog100nimbusblue.jpg"));

    verify(variantService).findAllByProductId(1L);
  }

  @Test
  @DisplayName(
      "GET /products/{productId}/variants - Given product does not exist, when find all, then returns not found")
  void givenProductDoesNotExist_whenFindAllVariants_thenReturnsNotFound() throws Exception {
    when(variantService.findAllByProductId(anyLong()))
        .thenThrow(new ResourceNotFoundException(MessageKeys.PRODUCT_NOT_FOUND_EXCEPTION));

    mockMvc
        .perform(get("/products/{productId}/variants", 999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Product not found"))
        .andExpect(jsonPath("$.instance").value("/products/999/variants"));
  }

  @Test
  @DisplayName(
      "GET /products/{productId}/variants/{variantId} - Given variant exists, when find by id, then returns variant")
  void givenVariantExists_whenFindVariantById_thenReturnsVariant() throws Exception {
    when(variantService.findById(anyLong(), anyLong())).thenReturn(variant);

    mockMvc
        .perform(
            get("/products/{productId}/variants/{variantId}", 1L, 1L)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.sku").value("motog100nimbusblue"))
        .andExpect(jsonPath("$.compared_at_price").value(9999.00))
        .andExpect(jsonPath("$.attributes.length()").value(1))
        .andExpect(jsonPath("$.images.length()").value(1));

    verify(variantService).findById(1L, 1L);
  }

  @Test
  @DisplayName(
      "GET /products/{productId}/variants/{variantId} - Given product does not exist, when find by id, then returns product not found")
  void givenProductDoesNotExist_whenFindVariantById_thenReturnsProductNotFound() throws Exception {
    when(variantService.findById(anyLong(), anyLong()))
        .thenThrow(new ResourceNotFoundException(MessageKeys.PRODUCT_NOT_FOUND_EXCEPTION));

    mockMvc
        .perform(get("/products/{productId}/variants/{variantId}", 999L, 1L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Product not found"))
        .andExpect(jsonPath("$.instance").value("/products/999/variants/1"));
  }

  @Test
  @DisplayName(
      "GET /products/{productId}/variants/{variantId} - Given variant does not exist, when find by id, then returns variant not found")
  void givenVariantDoesNotExist_whenFindVariantById_thenReturnsVariantNotFound() throws Exception {
    when(variantService.findById(anyLong(), anyLong()))
        .thenThrow(new ResourceNotFoundException(MessageKeys.PRODUCT_VARIANT_NOT_FOUND_EXCEPTION));

    mockMvc
        .perform(get("/products/{productId}/variants/{variantId}", 1L, 999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Product variant not found"))
        .andExpect(jsonPath("$.instance").value("/products/1/variants/999"));
  }
}
