package com.carlosarroyoam.ecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.ecommerce.core.constant.MessageKeys;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.product.dto.VariantResponse;
import com.carlosarroyoam.ecommerce.product.entity.Attribute;
import com.carlosarroyoam.ecommerce.product.entity.Variant;
import com.carlosarroyoam.ecommerce.product.entity.VariantAttributeValue;
import com.carlosarroyoam.ecommerce.product.entity.VariantImage;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VariantServiceTest {
  @Mock private ProductRepository productRepository;
  @Mock private VariantRepository variantRepository;
  @InjectMocks private VariantService variantService;

  private Variant variant;

  @BeforeEach
  void setUp() {
    variant =
        Variant.builder()
            .id(1L)
            .sku("motog100nimbusblue")
            .price(new BigDecimal("8999.00"))
            .comparedAtPrice(new BigDecimal("9999.00"))
            .costPerItem(new BigDecimal("7000.00"))
            .quantityOnStock(23)
            .build();
    variant.setAttributes(
        List.of(
            VariantAttributeValue.builder()
                .id(1L)
                .value("Blue Nimbus")
                .variant(variant)
                .attribute(Attribute.builder().id(1L).title("Color").build())
                .build()));
    variant.setImages(
        List.of(
            VariantImage.builder().id(1L).url("motog100nimbusblue.jpg").variant(variant).build()));
  }

  @Test
  @DisplayName("Given product with variants, when find all by product id, then returns variants")
  void givenProductWithVariants_whenFindAllByProductId_thenReturnsVariants() {
    when(productRepository.existsById(anyLong())).thenReturn(true);
    when(variantRepository.findAllByProductId(anyLong())).thenReturn(List.of(variant));

    List<VariantResponse> response = variantService.findAllByProductId(1L);

    assertThat(response)
        .hasSize(1)
        .first()
        .satisfies(item -> {
          assertThat(item.getId()).isEqualTo(1L);
          assertThat(item.getSku()).isEqualTo("motog100nimbusblue");
          assertThat(item.getPrice()).isEqualByComparingTo("8999.00");
          assertThat(item.getComparedAtPrice()).isEqualByComparingTo("9999.00");
          assertThat(item.getAttributes()).hasSize(1);
          assertThat(item.getAttributes().get(0).getValue()).isEqualTo("Blue Nimbus");
          assertThat(item.getAttributes().get(0).getAttribute().getTitle()).isEqualTo("Color");
          assertThat(item.getImages()).extracting("url").containsExactly("motog100nimbusblue.jpg");
        });
    verify(variantRepository).findAllByProductId(1L);
  }

  @Test
  @DisplayName(
      "Given product without variants, when find all by product id, then returns empty list")
  void givenProductWithoutVariants_whenFindAllByProductId_thenReturnsEmptyList() {
    when(productRepository.existsById(anyLong())).thenReturn(true);
    when(variantRepository.findAllByProductId(anyLong())).thenReturn(List.of());

    List<VariantResponse> response = variantService.findAllByProductId(1L);

    assertThat(response).isEmpty();
  }

  @Test
  @DisplayName(
      "Given product does not exist, when find all by product id, then throws product not found exception")
  void givenProductDoesNotExist_whenFindAllByProductId_thenThrowsProductNotFoundException() {
    when(productRepository.existsById(anyLong())).thenReturn(false);

    assertThatThrownBy(() -> variantService.findAllByProductId(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(MessageKeys.PRODUCT_NOT_FOUND_EXCEPTION);

    verify(variantRepository, never()).findAllByProductId(anyLong());
  }

  @Test
  @DisplayName("Given variant belongs to product, when find by id, then returns variant")
  void givenVariantBelongsToProduct_whenFindById_thenReturnsVariant() {
    when(productRepository.existsById(anyLong())).thenReturn(true);
    when(variantRepository.findByIdAndProductId(anyLong(), anyLong()))
        .thenReturn(Optional.of(variant));

    VariantResponse response = variantService.findById(1L, 1L);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getSku()).isEqualTo("motog100nimbusblue");
    assertThat(response.getAttributes()).hasSize(1);
    assertThat(response.getImages()).hasSize(1);
    verify(variantRepository).findByIdAndProductId(1L, 1L);
  }

  @Test
  @DisplayName(
      "Given product does not exist, when find by id, then throws product not found exception")
  void givenProductDoesNotExist_whenFindById_thenThrowsProductNotFoundException() {
    when(productRepository.existsById(anyLong())).thenReturn(false);

    assertThatThrownBy(() -> variantService.findById(999L, 1L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(MessageKeys.PRODUCT_NOT_FOUND_EXCEPTION);

    verify(variantRepository, never()).findByIdAndProductId(anyLong(), anyLong());
  }

  @Test
  @DisplayName(
      "Given variant does not belong to product, when find by id, then throws variant not found exception")
  void givenVariantDoesNotBelongToProduct_whenFindById_thenThrowsVariantNotFoundException() {
    when(productRepository.existsById(anyLong())).thenReturn(true);
    when(variantRepository.findByIdAndProductId(anyLong(), anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> variantService.findById(1L, 3L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(MessageKeys.PRODUCT_VARIANT_NOT_FOUND_EXCEPTION);

    verify(variantRepository).findByIdAndProductId(3L, 1L);
  }
}
