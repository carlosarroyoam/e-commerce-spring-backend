package com.carlosarroyoam.ecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.ecommerce.category.entity.Category;
import com.carlosarroyoam.ecommerce.core.constant.MessageKeys;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.product.dto.ProductResponse;
import com.carlosarroyoam.ecommerce.product.dto.ProductSpecs;
import com.carlosarroyoam.ecommerce.product.entity.Attribute;
import com.carlosarroyoam.ecommerce.product.entity.Product;
import com.carlosarroyoam.ecommerce.product.entity.ProductPropertyValue;
import com.carlosarroyoam.ecommerce.product.entity.Property;
import com.carlosarroyoam.ecommerce.product.entity.Variant;
import com.carlosarroyoam.ecommerce.product.entity.VariantAttributeValue;
import com.carlosarroyoam.ecommerce.product.entity.VariantImage;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
  @Mock private ProductRepository productRepository;
  @Spy private Clock clock = Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneOffset.UTC);
  @InjectMocks private ProductService productService;

  private Product product;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now(clock);

    Category category =
        Category.builder().id((byte) 1).title("Smartphones").slug("smartphones").build();
    Property brand = Property.builder().id(1L).title("Brand").build();
    Attribute color = Attribute.builder().id(1L).title("Color").build();

    product =
        Product.builder()
            .id(1L)
            .title("Moto G100")
            .slug("moto-g100")
            .description("Moto G100")
            .isFeatured(false)
            .isActive(true)
            .category(category)
            .createdAt(now.minusDays(1))
            .updatedAt(now.minusDays(1))
            .build();

    Variant variant =
        Variant.builder()
            .id(1L)
            .sku("motog100nimbusblue")
            .price(new BigDecimal("8999.00"))
            .comparedAtPrice(new BigDecimal("9999.00"))
            .costPerItem(new BigDecimal("7000.00"))
            .quantityOnStock(23)
            .product(product)
            .build();
    variant.setAttributes(
        List.of(
            VariantAttributeValue.builder()
                .id(1L)
                .value("Blue Nimbus")
                .variant(variant)
                .attribute(color)
                .build()));
    variant.setImages(
        List.of(
            VariantImage.builder().id(1L).url("motog100nimbusblue.jpg").variant(variant).build()));

    product.setProperties(
        List.of(
            ProductPropertyValue.builder()
                .id(1L)
                .value("Motorola")
                .product(product)
                .property(brand)
                .build()));
    product.setVariants(List.of(variant));
  }

  @Test
  @DisplayName("Given products exist, when find all, then returns paged products")
  void givenProductsExist_whenFindAll_thenReturnsPagedProducts() {
    ProductSpecs productSpecs = ProductSpecs.builder().title("Moto").isActive(true).build();
    Pageable pageable = PageRequest.of(0, 10);
    when(productRepository.findAll(
            ArgumentMatchers.<Specification<Product>>any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(product), pageable, 1));

    PagedResponse<ProductResponse> response = productService.findAll(productSpecs, pageable);

    assertThat(response.getItems())
        .hasSize(1)
        .first()
        .satisfies(item -> {
          assertThat(item.getId()).isEqualTo(1L);
          assertThat(item.getTitle()).isEqualTo("Moto G100");
          assertThat(item.getSlug()).isEqualTo("moto-g100");
          assertThat(item.getIsFeatured()).isFalse();
          assertThat(item.getIsActive()).isTrue();
          assertThat(item.getCategory().getTitle()).isEqualTo("Smartphones");
        });
    assertThat(response.getPagination().getPage()).isZero();
    assertThat(response.getPagination().getSize()).isEqualTo(10);
    assertThat(response.getPagination().getTotalItems()).isEqualTo(1);
    assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
  }

  @Test
  @DisplayName("Given all filters, when find all, then builds specification and queries repository")
  void givenAllFilters_whenFindAll_thenBuildsSpecificationAndQueriesRepository() {
    ProductSpecs productSpecs =
        ProductSpecs.builder()
            .title("moto")
            .slug("moto-g100")
            .isFeatured(false)
            .isActive(true)
            .categoryId((byte) 1)
            .startDate(LocalDate.of(2025, 1, 1))
            .endDate(LocalDate.of(2025, 12, 31))
            .build();
    Pageable pageable = PageRequest.of(0, 10);
    when(productRepository.findAll(
            ArgumentMatchers.<Specification<Product>>any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(), pageable, 0));

    PagedResponse<ProductResponse> response = productService.findAll(productSpecs, pageable);

    assertThat(response.getItems()).isEmpty();
    assertThat(response.getPagination().getTotalItems()).isZero();
    ArgumentCaptor<Specification<Product>> specCaptor = ArgumentCaptor.captor();
    verify(productRepository).findAll(specCaptor.capture(), eq(pageable));
    assertThat(specCaptor.getValue()).isNotNull();
  }

  @Test
  @DisplayName("Given product exists, when find by id, then returns product with details")
  void givenProductExists_whenFindById_thenReturnsProductWithDetails() {
    when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

    ProductResponse response = productService.findById(1L);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getTitle()).isEqualTo("Moto G100");
    assertThat(response.getCategory().getSlug()).isEqualTo("smartphones");
    assertThat(response.getProperties()).hasSize(1);
    assertThat(response.getProperties().get(0).getValue()).isEqualTo("Motorola");
    assertThat(response.getProperties().get(0).getProperty().getTitle()).isEqualTo("Brand");
    assertThat(response.getVariants()).hasSize(1);
    assertThat(response.getVariants().get(0).getSku()).isEqualTo("motog100nimbusblue");
    assertThat(response.getVariants().get(0).getAttributes()).hasSize(1);
    assertThat(response.getVariants().get(0).getImages()).hasSize(1);
  }

  @Test
  @DisplayName("Given product does not exist, when find by id, then throws not found exception")
  void givenProductDoesNotExist_whenFindById_thenThrowsNotFoundException() {
    when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.findById(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(MessageKeys.PRODUCT_NOT_FOUND_EXCEPTION);
  }
}
